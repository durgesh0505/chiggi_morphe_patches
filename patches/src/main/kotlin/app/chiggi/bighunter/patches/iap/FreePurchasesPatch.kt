package app.chiggi.bighunter.patches.iap

import app.chiggi.bighunter.patches.shared.Constants.COMPATIBILITY_BIG_HUNTER
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val freePurchasesPatch = bytecodePatch(
    name = "Free in-app purchases",
    description = "Makes every in-app purchase free and unlimited. KKJPaymentGoogle.startPurchase(key) " +
        "normally opens the Google Play billing flow; this redirects it straight to the native grant " +
        "applyProduct(key), skipping payment. There is no client receipt check (verifyDeveloperPayload " +
        "returns true) and no server validation, so the product is granted for real — consumables " +
        "(coins) can be re-bought unlimitedly. NOTE: no money changes hands and nothing is sent to " +
        "Google; this only works because the grant is client-side.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BIG_HUNTER)

    execute {
        // p1 = product key. Grant natively and skip launchBillingFlow entirely.
        StartPurchaseFingerprint.method.addInstructions(0, """
            invoke-static {p1}, LkakarodJavaLibs/data/KKJPaymentGoogle;->applyProduct(Ljava/lang/String;)V
            return-void
        """)
    }
}
