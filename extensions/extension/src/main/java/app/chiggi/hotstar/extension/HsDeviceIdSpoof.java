package app.chiggi.hotstar.extension;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Supplies a stable-per-install random device id to replace the value JioHotstar's DeviceInfoStore
 * derives from Settings.Secure ANDROID_ID.
 *
 * <p>ANDROID_ID is keyed to the app signing key and survives an app-data clear, so a re-signed build
 * always sends the same X-HS-Device-Id header (and the same /v2/start body device ids). Once that
 * device id is flagged, the server returns the "device logged out" page on every launch and clearing
 * data does not help. Returning a random UUID here makes the app present as a device the server has
 * never seen; the value is persisted so it stays stable across launches, and regenerates on an
 * app-data clear (giving a fresh device if a previous one gets flagged again).
 *
 * <p>If an Application context can't be reached (very early startup), a per-process random value is
 * used as a fallback so the call never fails.
 */
public final class HsDeviceIdSpoof {

    private static final Object LOCK = new Object();
    private static final String FALLBACK = UUID.randomUUID().toString();

    private static SharedPreferences prefs;
    private static String deviceId;

    private HsDeviceIdSpoof() {
    }

    /** Random device id, stable per install, replacing ANDROID_ID. */
    public static String deviceId() {
        synchronized (LOCK) {
            try {
                if (deviceId != null) {
                    return deviceId;
                }
                Context context = appContext();
                if (context == null) {
                    return FALLBACK;
                }
                if (prefs == null) {
                    prefs = context.getSharedPreferences("hs_devid_spoof", Context.MODE_PRIVATE);
                }
                String id = prefs.getString("id", null);
                if (id == null || id.isEmpty()) {
                    id = UUID.randomUUID().toString();
                    prefs.edit().putString("id", id).apply();
                }
                deviceId = id;
                return id;
            } catch (Throwable t) {
                return FALLBACK;
            }
        }
    }

    private static Context appContext() {
        try {
            Method m = Class.forName("android.app.ActivityThread").getMethod("currentApplication");
            return (Context) m.invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
