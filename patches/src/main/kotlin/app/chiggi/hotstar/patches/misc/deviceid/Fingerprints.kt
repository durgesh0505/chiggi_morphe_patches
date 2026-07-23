package app.chiggi.hotstar.patches.misc.deviceid

import app.morphe.patcher.Fingerprint

/**
 * com.hotstar.core.commonutils.stores.DeviceInfoStore#getDeviceId() (R8-renamed to `a`).
 *
 * The suspend device-id getter: reads the persisted value, and when empty derives it from
 * Settings.Secure ANDROID_ID (the only android_id read in the whole app), caching it. This single
 * method is the source of BOTH the `X-HS-Device-Id` request header (CommonHeaderInterceptor) and the
 * `/v2/start` body device-id entries (com.hotstar.bff.utils.c calls it twice). Forcing it to return a
 * spoofed id therefore replaces the device identity everywhere the server keys on it.
 *
 * It is the only method on DeviceInfoStore that takes a raw `Tj.a` (kotlin Continuation) parameter
 * and returns Object — the sibling suspend methods (b/c/d/e) take `ContinuationImpl` — so this anchors
 * on definingClass + returnType + that parameter. Pinned to 26.06.22.3.
 */
internal object DeviceIdGetterFingerprint : Fingerprint(
    definingClass = "Lcom/hotstar/core/commonutils/stores/DeviceInfoStore;",
    name = "a",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("LTj/a;"),
)
