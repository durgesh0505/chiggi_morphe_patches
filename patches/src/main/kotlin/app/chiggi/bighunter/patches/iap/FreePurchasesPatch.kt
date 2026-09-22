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
        // p0 = this, p1 = product key. Drive the app's OWN successful-purchase flow in the exact order
        // and native context a real purchase uses, instead of launchBillingFlow:
        //   disableUserUI()  -> native startProcess() (the "purchase in progress" scene lock that the
        //                       native applyProduct grant appears to require)
        //   addAndSaveUserPurchasedKey(key) + buyedProductKeyList.add(key)  -> mark owned/persist __upk
        //   notifyPurchased() -> (app) enableUserUI -> applyPurchasedProducts -> applyProduct(key) on
        //                        the UI thread, then stopProcess() to unlock.
        // A shortcut applyProduct() WITHOUT the startProcess bracket did not credit currency natively.
        // enableUserUI always calls stopProcess at the end, so the scene never stays locked even if the
        // native grant is a no-op.
        StartPurchaseFingerprint.method.addInstructions(0, """
            invoke-virtual {p0}, LkakarodJavaLibs/data/KKJPaymentGoogle;->disableUserUI()V
            invoke-static {p1}, LkakarodJavaLibs/data/KKJPaymentGoogle;->addAndSaveUserPurchasedKey(Ljava/lang/String;)V
            iget-object v0, p0, LkakarodJavaLibs/data/KKJPaymentGoogle;->buyedProductKeyList:Ljava/util/ArrayList;
            invoke-virtual {v0, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
            invoke-direct {p0}, LkakarodJavaLibs/data/KKJPaymentGoogle;->notifyPurchased()V
            return-void
        """)
    }
}
