package app.chiggi.bighunter.patches.iap

import app.morphe.patcher.Fingerprint

/**
 * KKJPaymentGoogle.startPurchase(String key) — the single buy entry (buyProductWithKey -> startPurchase),
 * normally leads to BillingClient.launchBillingFlow. The grant itself is the native call
 * applyProduct(String) and there is NO client receipt check (verifyDeveloperPayload() returns true) or
 * server validation, so redirecting startPurchase straight to applyProduct grants the product for free,
 * repeatably (consumables -> unlimited). Instance method: p0=this, p1=key.
 */
internal object StartPurchaseFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJPaymentGoogle;",
    name = "startPurchase",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)
