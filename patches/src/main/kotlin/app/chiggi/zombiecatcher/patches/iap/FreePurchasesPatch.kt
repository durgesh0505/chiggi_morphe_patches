package app.chiggi.zombiecatcher.patches.iap

import app.chiggi.zombiecatcher.patches.shared.Constants.COMPATIBILITY_ZOMBIE_CATCHERS
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS = "Lapp/chiggi/zombiecatcher/extension/ZombieCatchersPatch;"
private const val SERVICE_CLASS = "Lfi/twomenandadog/zombiecatchers/InAppServiceImpl;"

@Suppress("unused")
val freePurchasesPatch = bytecodePatch(
    name = "Free in-app purchases",
    description = "Skips Google Play Billing and exposes the app's product list at zero price. " +
        "Buying an item sends a synthetic purchase receipt through Zombie Catchers' own native " +
        "purchaseResult success callback, and the native server receipt check is forced off so " +
        "the grant never depends on the developer's verification endpoint. No payment reaches " +
        "Google. The developer's own event log still records each purchase; use a throwaway account.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZOMBIE_CATCHERS)
    dependsOn(disableIapVerificationPatch)
    extendWith("extensions/extension.mpe")

    execute {
        InitInAppServiceFingerprint.method.addInstructions(0, """
            iput-object p1, p0, $SERVICE_CLASS->mActivity:Lfi/twomenandadog/zombiecatchers/ZCActivity;
            return-void
        """)
        ConnectStoreFingerprint.method.addInstructions(0, """
            iget-object v0, p0, $SERVICE_CLASS->mActivity:Lfi/twomenandadog/zombiecatchers/ZCActivity;
            invoke-static {v0, p1, p2}, $EXTENSION_CLASS->connectStore(Ljava/lang/Object;J)V
            return-void
        """)
        QueryInventoryFingerprint.method.addInstructions(0, """
            iget-object v0, p0, $SERVICE_CLASS->mActivity:Lfi/twomenandadog/zombiecatchers/ZCActivity;
            invoke-static {v0, p1, p2, p3}, $EXTENSION_CLASS->queryInventory(Ljava/lang/Object;[Ljava/lang/String;J)V
            return-void
        """)
        PurchaseFingerprint.method.addInstructions(0, """
            iget-object v0, p0, $SERVICE_CLASS->mActivity:Lfi/twomenandadog/zombiecatchers/ZCActivity;
            invoke-static {v0, p1, p2, p3}, $EXTENSION_CLASS->grantPurchase(Ljava/lang/Object;Ljava/lang/String;J)V
            return-void
        """)
    }
}
