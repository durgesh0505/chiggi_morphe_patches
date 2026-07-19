package app.chiggi.hotstar.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Supplies randomized, slowly-rotating device identifiers for JioHotstar's device-attestation blob
 * (Kg/b), so a device Hotstar has blocklisted (by its GAID / Widevine id) presents as a different
 * device.
 *
 * <p>The ids are per-install random and persisted in SharedPreferences, and rotate once every
 * {@link #ROTATE_MS} (24h). That keeps them stable across launches and through a whole day — so they
 * look like a real, stable device id rather than one that mutates every launch — while still rotating
 * to auto-escape a blocklist. Because the value is stored per install, two devices don't collide.
 *
 * <p>Only the value REPORTED in the fraud/attestation blob is changed. The real Widevine device used
 * for DRM licensing is obtained separately at the MediaDrm layer, so playback is unaffected.
 *
 * <p>If an Application context can't be reached (very early startup), a per-process random value
 * is used as a fallback so the call never fails.
 */
public final class DeviceIdSpoof {


    // Rotate once a day. Change to 12h with 12L if you want faster recovery from a re-block.
    private static final long ROTATE_MS = 24L * 60L * 60L * 1000L;

    private static final Object LOCK = new Object();

    private static final String FALLBACK_GAID = UUID.randomUUID().toString();
    private static final String FALLBACK_WIDEVINE = randomWidevine();

    private static SharedPreferences prefs;
    private static String gaid;
    private static String widevine;
    private static long rotatedAt;

    private DeviceIdSpoof() {
    }

    /** Randomized Google Advertising ID (UUID form), stable per install, rotating daily. */
    public static String gaid() {
        ensureFresh();
        return gaid != null ? gaid : FALLBACK_GAID;
    }

    /** Randomized Widevine device id (base64 of 32 random bytes), stable per install. */
    public static String widevineId() {
        ensureFresh();
        return widevine != null ? widevine : FALLBACK_WIDEVINE;
    }

    private static void ensureFresh() {
        synchronized (LOCK) {
            try {
                if (prefs == null) {
                    Context context = appContext();
                    if (context == null) {
                        return; // use the per-process fallback
                    }
                    prefs = context.getSharedPreferences("hs_dev_spoof", Context.MODE_PRIVATE);
                    gaid = prefs.getString("gaid", null);
                    widevine = prefs.getString("wv", null);
                    rotatedAt = prefs.getLong("ts", 0L);
                }
                long now = System.currentTimeMillis();
                if (gaid == null || widevine == null || now - rotatedAt > ROTATE_MS || rotatedAt > now) {
                    gaid = UUID.randomUUID().toString();
                    widevine = randomWidevine();
                    rotatedAt = now;
                    prefs.edit()
                        .putString("gaid", gaid)
                        .putString("wv", widevine)
                        .putLong("ts", rotatedAt)
                        .apply();
                }
            } catch (Throwable ignored) {
                // Never break the host app; the fallback values are returned.
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

    private static String randomWidevine() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}