package app.chiggi.crazygames.extension;

import java.lang.reflect.Method;

/**
 * Completes a Capacitor AdMob modal ad without showing it. The "Remove ads" patch injects a call to
 * {@link #fireAdEvents} at the top of the AdMob plugin's show methods
 * ({@code com.getcapacitor.community.admob.AdMob.showInterstitial / showRewardVideoAd /
 * showRewardInterstitialAd}), replacing the ad-show body.
 *
 * <p>The CrazyGames WebView drives each modal ad through a two-event state machine and keeps a
 * loading overlay up until the terminal event arrives:
 * <pre>
 *   // interstitial
 *   AdMob.addListener(Showed,    () =&gt; onAdStarted());
 *   AdMob.addListener(Dismissed, () =&gt; onAdFinished());          // hides the loading overlay
 *   // rewarded
 *   let earned = false;
 *   AdMob.addListener(Rewarded,  () =&gt; { earned = true; });
 *   AdMob.addListener(Dismissed, () =&gt; earned ? onAdFinished()   // credits reward + hides loading
 *                                              : onAdError("dismissed before reward"));
 * </pre>
 * If only the first event is fired (or none), the overlay never dismisses and the game hangs on the
 * loading screen. So this fires both events, in order (Showed/Reward then Dismissed), then resolves
 * the PluginCall so the WebView's {@code await} settles — exactly as a real, fully-watched ad would,
 * but instantly and with no video.
 *
 * <p>Everything is reflection wrapped in try/catch, so any R8 rename mismatch degrades to a no-op
 * instead of crashing the host app.
 */
public final class AdRewardPatch {

    private AdRewardPatch() {
    }

    /**
     * Fires two Capacitor listener events (in order) and resolves the call, completing a modal ad
     * without showing it.
     *
     * @param plugin       the AdMob plugin instance (a Capacitor Plugin); injected as {@code p0}.
     * @param call         the PluginCall for the ad request; injected as {@code p1}.
     * @param firstEvent   the lifecycle event to fire first (interstitial Showed, or rewarded Reward).
     * @param dismissEvent the terminal "dismissed" event that finishes the ad and hides the overlay.
     */
    public static void fireAdEvents(Object plugin, Object call, String firstEvent, String dismissEvent) {
        try {
            // Plugin.notifyListeners(String, JSObject) keeps its name across R8. Walk up the class
            // hierarchy and derive the JSObject class from its second parameter so the obfuscated
            // JSObject name is never hard-coded.
            Method notify = findNotifyListeners(plugin.getClass());
            if (notify == null) {
                return;
            }
            notify.setAccessible(true);
            Class<?> jsObjectClass = notify.getParameterTypes()[1];

            // Build a payload with the framework org.json.JSONObject.put (Capacitor's JSObject
            // extends JSONObject), so the put helpers are not obfuscation-sensitive. The web ignores
            // the payload (its handlers take no args), but include it to mirror a real reward.
            Object data = jsObjectClass.getDeclaredConstructor().newInstance();
            try {
                jsObjectClass.getMethod("put", String.class, Object.class)
                    .invoke(data, "type", "rewarded");
                jsObjectClass.getMethod("put", String.class, int.class)
                    .invoke(data, "amount", 1);
            } catch (Throwable ignored) {
                // Payload is best-effort; the events below are what matter.
            }

            // 1) First event -> the web marks the ad as started (interstitial) or earned (rewarded).
            notify.invoke(plugin, firstEvent, data);
            // 2) Dismissed event -> the web finishes the ad and hides the loading overlay.
            notify.invoke(plugin, dismissEvent, data);
            // 3) Settle the show... promise so the WebView's await does not hang.
            resolveWith(call, jsObjectClass, data);
        } catch (Throwable ignored) {
            // Graceful no-op: never crash the host app.
        }
    }

    private static Method findNotifyListeners(Class<?> from) {
        for (Class<?> c = from; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals("notifyListeners")) {
                    Class<?>[] params = m.getParameterTypes();
                    if (params.length == 2 && params[0] == String.class) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    private static void resolveWith(Object call, Class<?> jsObjectClass, Object data) {
        try {
            for (Method m : call.getClass().getMethods()) {
                Class<?>[] params = m.getParameterTypes();
                if (m.getReturnType() == void.class
                    && params.length == 1
                    && params[0] == jsObjectClass) {
                    m.invoke(call, data);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // Resolving is best-effort; the Dismissed event has already finished the flow.
        }
    }
}
