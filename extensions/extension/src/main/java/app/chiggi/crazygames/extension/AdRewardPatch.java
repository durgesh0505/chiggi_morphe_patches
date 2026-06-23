package app.chiggi.crazygames.extension;

import java.lang.reflect.Method;

/**
 * Auto-grants a Capacitor AdMob rewarded ad without showing it. The "Remove ads" patch injects a
 * call to {@link #grantReward(Object, Object, String)} at the top of
 * {@code com.getcapacitor.community.admob.AdMob.showRewardVideoAd} and
 * {@code showRewardInterstitialAd}, replacing the ad-show body.
 *
 * <p>The CrazyGames WebView grants the in-app reward when it receives the native reward listener
 * event ({@code onRewardedVideoAdReward} / {@code onRewardedInterstitialAdReward}). This replicates
 * exactly what the native OnUserEarnedReward callback does — build a JSObject {type, amount}, fire
 * the listener event and resolve the PluginCall — but immediately, with no video shown.
 *
 * <p>Everything is reflection wrapped in try/catch, so any R8 rename mismatch degrades to a silent
 * no-op (the reward simply isn't granted) instead of crashing the host app.
 */
public final class AdRewardPatch {

    private AdRewardPatch() {
    }

    /**
     * @param plugin the AdMob plugin instance (a Capacitor Plugin); injected as {@code p0}.
     * @param call   the PluginCall for the rewarded request; injected as {@code p1}.
     * @param event  the reward listener event name to fire.
     */
    public static void grantReward(Object plugin, Object call, String event) {
        try {
            // Plugin.notifyListeners(String, JSObject) keeps its name across R8. Walk up the class
            // hierarchy (it is declared on the obfuscated Plugin base) and derive the JSObject class
            // from its second parameter, so the obfuscated JSObject name is never hard-coded.
            Method notify = findNotifyListeners(plugin.getClass());
            if (notify == null) {
                return;
            }
            Class<?> jsObjectClass = notify.getParameterTypes()[1];

            // Build the reward payload with the framework org.json.JSONObject.put (Capacitor's
            // JSObject extends JSONObject), so the put helpers are not obfuscation-sensitive.
            Object reward = jsObjectClass.getDeclaredConstructor().newInstance();
            jsObjectClass.getMethod("put", String.class, Object.class)
                .invoke(reward, "type", "rewarded");
            jsObjectClass.getMethod("put", String.class, int.class)
                .invoke(reward, "amount", 1);

            // Fire the reward listener event — this is what the web grants on.
            notify.setAccessible(true);
            notify.invoke(plugin, event, reward);

            // Resolve the PluginCall with the reward so the WebView's await settles (the single
            // public void method taking one JSObject = PluginCall.resolve(JSObject)).
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

    private static void resolveWith(Object call, Class<?> jsObjectClass, Object reward)
        throws Exception {
        for (Method m : call.getClass().getMethods()) {
            Class<?>[] params = m.getParameterTypes();
            if (m.getReturnType() == void.class
                && params.length == 1
                && params[0] == jsObjectClass) {
                m.invoke(call, reward);
                return;
            }
        }
    }
}
