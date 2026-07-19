package app.chiggi.commute.patches.premium

import app.morphe.patcher.Fingerprint

/**
 * com.mediocre.commute.AndroidStore — the Java Play-Billing bridge the native game (libcommute.so)
 * drives through the MainActivity.command JNI dispatcher. The native side decides whether premium is
 * owned by sending "storeisrestored,com.mediocre.commute.premium", which routes to
 * isProductIdRestored() and returns "true"/"false", and it reads the store-operation result via
 * "storegetstatus" -> getStatusAsString() (where "2" == STORE_SUCCEEDED). Class and method names are
 * not obfuscated, so these anchor on definingClass + name.
 */

/**
 * isProductIdRestored(String) already just returns the mIsPremiumProductRestored flag (it ignores the
 * sku argument). Forcing it to return true makes the native ownership check see premium as owned,
 * with no real Play purchase.
 */
internal object IsProductIdRestoredFingerprint : Fingerprint(
    definingClass = "Lcom/mediocre/commute/AndroidStore;",
    name = "isProductIdRestored",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
)

/**
 * getStatusAsString() returns the store-operation status ("1" waiting / "2" succeeded / "3" failed).
 * Forcing "2" makes the native restore/purchase flow see the operation as succeeded so it proceeds to
 * (and trusts) the ownership check above, even when no real purchase/restore happened.
 */
internal object GetStatusAsStringFingerprint : Fingerprint(
    definingClass = "Lcom/mediocre/commute/AndroidStore;",
    name = "getStatusAsString",
    returnType = "Ljava/lang/String;",
)
