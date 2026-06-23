package app.chiggi.crazygames.extension;

import java.lang.reflect.Method;

/**
 * Auto-grants a Capacitor AdMob rewarded ad without showing it. The "Remove ads" patch injects a
 * call to {@link #grantReward} at the top of {@code com.getcapacitor.community.admob.AdMob.showRewardVideoAd}
 * and {@code showRewardInterstitialAd}, replacing the ad-show body.
 *
 * <p>The CrazyGames WebView drives a two-event state machine for rewarded ads:
 * <pre>
 *   let earned = false;
 *   AdMob.addListener(Rewarded,  () =&gt; { earned = true; });
 *   AdMob.addListener(Dismissed, () =&gt; earned ? onAdFinished()   // grants reward + hides the loading screen
 *                                              : onAdError("dismissed before reward"));
 * </pre>
 * So the reward is credited and the loading overlay is dismissed on the <b>Dismissed</b> event, but
 * only if the <b>Reward</b> event fired first. This fires both, in that order, then resolves the
 * PluginCall so the WebView's {@code await} settles — exactly as a fully-watched ad would, but with
 * no video.
 *
 * <p>Everything is reflection wrapped in try/catch, so any R8 rename mismatch degrades to a no-op
 * instead of crashing the host app.
 */
public final class AdRewardPatch {

    private AdRewardPatch() {
    }

    /**
     * @param plugin       the AdMob plugin instance (a Capacitor Plugin); injected as {@code p0}.
     * @param call         the PluginCall for the rewarded request; injected as {@code p1}.
     * @param rewardEvent  the reward listener event name (sets the "earned" flag).
     * @param dismissEvent the dismissed listener event name (finishes: grants + hides loading).
     */
    public static void grantReward(Object plugin, Object call, String rewardEvent, String dismissEvent) {
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

            // Build the reward payload with the framework org.json.JSONObject.put (Capacitor's
            // JSObject extends JSONObject), so the put helpers are not obfuscation-sensitive. The
            // web ignores the payload, but include it to mirror a real reward.
            Object reward = jsObjectClass.getDeclaredConstructor().newInstance();
            try {
                jsObjectClass.getMethod("put", String.class, Object.class)
                    .invoke(reward, "type", "rewarded");
                jsObjectClass.getMethod("put", String.class, int.class)
                    .invoke(reward, "amount", 1);
            } catch (Throwable ignored) {
                // Payload is best-effort; the events below are what matter.
            }

            // 1) Reward event -> the web marks the ad as "earned".
            notify.invoke(plugin, rewardEvent, reward);
            // 2) Dismissed event -> the web finishes: credits the reward and hides the loading screen.
            notify.invoke(plugin, dismissEvent, reward);
            // 3) Settle the showReward... promise so the WebView's await does not hang.
            resolveWith(call, jsObjectClass, reward);
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

    private static void resolveWith(Object call, Class<?> jsObjectClass, Object reward) {
        try {
            for (Method m : call.getClass().getMethods()) {
                Class<?>[] params = m.getParameterTypes();
                if (m.getReturnType() == void.class
                    && params.length == 1
                    && params[0] == jsObjectClass) {
                    m.invoke(call, reward);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // Resolving is best-effort; the Dismissed event has already finished the flow.
        }
    }
}
