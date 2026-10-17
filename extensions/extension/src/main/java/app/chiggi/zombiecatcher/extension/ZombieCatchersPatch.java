package app.chiggi.zombiecatcher.extension;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/** Runtime bridges used by the Zombie Catchers Morphe patches. */
public final class ZombieCatchersPatch {
    private static final String ORIGINAL_PACKAGE = "fi.twomenandadog.zombiecatchers";
    private static final String PRODUCT_CLASS = ORIGINAL_PACKAGE + ".inappcommon.Product";
    private static final String TAG = "MorpheZC";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ZombieCatchersPatch() {
    }

    /** Finishes an interstitial immediately so native game flow resumes without showing an ad. */
    public static void finishInterstitial(Object activity) {
        runOnMain("finishInterstitial", () -> finishAd(activity, "_interstitial", null));
    }

    /** Finishes a rewarded ad as successful so native reward logic can grant its reward. */
    public static void finishRewardedAd(Object activity) {
        runOnMain("finishRewardedAd", () -> finishAd(activity, "", Boolean.TRUE));
    }

    // Mirrors AdsNetworkServiceImpl: onAdDisplayed (didStart) then onAdHidden (videoEnded).
    private static void finishAd(Object activity, String suffix, Boolean rewarded) throws Exception {
        Object adService = getFieldValue(activity, "mAdsNetworkService");
        Class<?>[] callbackTypes = {boolean.class, boolean.class, long.class};

        setFieldValue(adService, "didStartVideoStatus" + suffix, true);
        invoke(activity, "AppLovinCallback", callbackTypes,
            false, true, getLong(adService, "didStartVideo" + suffix));

        boolean endedStatus = rewarded != null
            ? rewarded
            : (Boolean) getFieldValue(adService, "videoEndedStatus" + suffix);
        invoke(activity, "AppLovinCallback", callbackTypes,
            true, endedStatus, getLong(adService, "videoEnded" + suffix));
    }

    /** Reports a usable store connection without binding to Google Play Billing. */
    public static void connectStore(Object activity, long callback) {
        runOnMain("connectStore", () -> invoke(
            activity,
            "connectionResult",
            new Class<?>[]{boolean.class, String.class, long.class},
            true,
            "Morphe local purchase service",
            callback
        ));
    }

    /** Supplies all requested products with a zero display price. */
    public static void queryInventory(Object activity, String[] requestedProducts, long callback) {
        runOnMain("queryInventory", () -> sendInventory(activity, requestedProducts, callback));
    }

    private static void sendInventory(Object activity, String[] requestedProducts, long callback)
        throws Exception {
        Context context = (Context) activity;
        LinkedHashSet<String> productIds = new LinkedHashSet<>();
        if (requestedProducts != null) {
            for (String productId : requestedProducts) {
                if (productId != null && !productId.isEmpty()) {
                    productIds.add(productId);
                }
            }
        }

        Resources resources = context.getResources();
        int arrayId = resources.getIdentifier("inapp_purchase_list", "array", ORIGINAL_PACKAGE);
        if (arrayId == 0) {
            arrayId = resources.getIdentifier("inapp_purchase_list", "array", context.getPackageName());
        }
        if (arrayId != 0) {
            for (String productId : resources.getStringArray(arrayId)) {
                if (productId != null && !productId.isEmpty()) {
                    productIds.add(productId);
                }
            }
        }

        ClassLoader loader = activity.getClass().getClassLoader();
        Class<?> productClass = Class.forName(PRODUCT_CLASS, true, loader);
        Constructor<?> constructor = productClass.getConstructor(
            String.class,
            String.class,
            String.class,
            String.class,
            float.class,
            String.class,
            boolean.class,
            String.class,
            String.class
        );
        Object productArray = Array.newInstance(productClass, productIds.size());
        int index = 0;
        for (String productId : productIds) {
            Object product = constructor.newInstance(
                productId,
                productId,
                "Free local purchase",
                "$0.00",
                0.0f,
                "USD",
                false,
                "",
                ""
            );
            Array.set(productArray, index++, product);
        }

        invoke(
            activity,
            "queryResult",
            new Class<?>[]{boolean.class, productArray.getClass(), String.class, long.class},
            true,
            productArray,
            "",
            callback
        );
    }

    /** Sends a synthetic successful purchase through the app's existing native purchase callback. */
    public static void grantPurchase(Object activity, String productId, long callback) {
        runOnMain("grantPurchase", () -> sendPurchase(activity, productId, callback));
    }

    private static void sendPurchase(Object activity, String productId, long callback) throws Exception {
        String token = "morphe-" + UUID.randomUUID();
        String deviceId = "";
        try {
            Object value = invoke(activity, "retrieveDeviceId", new Class<?>[0]);
            if (value != null) {
                deviceId = value.toString();
            }
        } catch (Throwable ignored) {
            // Developer payload is compatibility metadata; success flow does not validate it.
        }

        JSONArray products = new JSONArray();
        products.put(productId);
        JSONObject receipt = new JSONObject();
        receipt.put("orderId", "MORPHE." + System.currentTimeMillis());
        receipt.put("packageName", ((Context) activity).getPackageName());
        receipt.put("productId", productId);
        receipt.put("productIds", products);
        receipt.put("purchaseTime", System.currentTimeMillis());
        receipt.put("purchaseState", 0);
        receipt.put("developerPayload", "ZC" + deviceId);
        receipt.put("purchaseToken", token);
        receipt.put("token", token);
        receipt.put("quantity", 1);
        receipt.put("acknowledged", true);

        invoke(
            activity,
            "purchaseResult",
            new Class<?>[]{boolean.class, boolean.class, String.class, String.class, long.class},
            true,
            false,
            receipt.toString(),
            token,
            callback
        );
    }

    private interface Action {
        void run() throws Exception;
    }

    // Billing and AppLovin deliver these callbacks on the main thread, never inside the native JNI call.
    private static void runOnMain(String name, Action action) {
        MAIN.post(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                Log.w(TAG, name + " failed", error);
            }
        });
    }

    private static long getLong(Object target, String name) throws Exception {
        Object value = getFieldValue(target, name);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private static void setFieldValue(Object target, String name, Object value) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                // Continue through the host class hierarchy.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object getFieldValue(Object target, String name) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                // Continue through the host class hierarchy.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args)
        throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Method method = type.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (NoSuchMethodException ignored) {
                // Continue through the host class hierarchy.
            }
        }
        throw new NoSuchMethodException(name);
    }
}
