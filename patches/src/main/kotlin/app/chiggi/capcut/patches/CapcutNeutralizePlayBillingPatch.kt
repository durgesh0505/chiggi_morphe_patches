package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.chiggi.capcut.patches.shared.Constants
import app.chiggi.capcut.patches.shared.disableComponentsByName
import app.chiggi.capcut.patches.shared.stripPermissionsWhere
import com.android.tools.smali.dexlib2.AccessFlags

private val capcutBillingResourcePatch = resourcePatch(
    name = "CapCut Play Billing Manifest Purge",
    description = "Strips the Play Billing permission and disables Play Billing activities in AndroidManifest.xml.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        val manifestFile = get("AndroidManifest.xml")
        if (!manifestFile.exists()) {
            println("[Neutralize Play Billing] Skipped: AndroidManifest.xml not found.")
            return@execute
        }

        document(manifestFile.absolutePath).use { document ->
            val application = document.documentElement.getElementsByTagName("application").item(0) as? org.w3c.dom.Element
            val stripped = document.documentElement.stripPermissionsWhere { it == "com.android.vending.BILLING" }
            val disabled = (application ?: document.documentElement).disableComponentsByName(
                "com.android.billingclient.api.ProxyBillingActivity",
                "com.android.billingclient.api.ProxyBillingActivityV2",
            )

            println("[Neutralize Play Billing] Stripped ${stripped.size} permission(s) and disabled $disabled component(s) in AndroidManifest.xml.")
        }
    }
}

private object BillingClientImplLaunchBillingFlowFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/BillingClientImpl;",
    name = "h",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Lcom/android/billingclient/api/BillingResult;",
    parameters = listOf("Landroid/app/Activity;", "Lcom/android/billingclient/api/BillingFlowParams;"),
)

private object BillingClientImplStartConnectionFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/BillingClientImpl;",
    name = "m",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingClientStateListener;"),
)

private object BillingClientImplIsReadyFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/BillingClientImpl;",
    name = "g",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object BillingClientImplIsFeatureSupportedFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/BillingClientImpl;",
    name = "f",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Lcom/android/billingclient/api/BillingResult;",
    parameters = listOf("Ljava/lang/String;"),
)

private object BillingClientImplQueryPurchasesFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/BillingClientImpl;",
    name = "k",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/QueryPurchasesParams;", "Lcom/android/billingclient/api/PurchasesResponseListener;"),
)

private object GoogleBillingServiceIsSupportGooglePayFingerprint : Fingerprint(
    definingClass = "Lcom/bytedance/pipo/iap/google/GoogleBillingService;",
    name = "isSupportGooglePay",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object GoogleIapExternalServiceProviderIsSupportGooglePayFingerprint : Fingerprint(
    definingClass = "Lcom/bytedance/pipo/iap/google/service/provider/GoogleIapExternalServiceProvider;",
    name = "isSupportGooglePay",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = listOf(),
)

@Suppress("unused")
val capcutNeutralizePlayBillingPatch = bytecodePatch(
    name = "Neutralize Play Billing",
    description = "Stubs Google Play Billing client calls and disables billing manifest components for graceful degradation on devices without GMS.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    dependsOn(capcutBillingResourcePatch)

    execute {
        var patched = 0

        // 1. launchBillingFlow returns BILLING_UNAVAILABLE (zzdb.b) immediately
        BillingClientImplLaunchBillingFlowFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/android/billingclient/api/zzdb;->b:Lcom/android/billingclient/api/BillingResult;
                return-object v0
            """,
        )
        patched++

        // 2. startConnection notifies listener with BILLING_UNAVAILABLE immediately
        BillingClientImplStartConnectionFingerprint.method.addInstructions(
            0,
            """
                if-eqz p1, :cond_null
                sget-object v0, Lcom/android/billingclient/api/zzdb;->b:Lcom/android/billingclient/api/BillingResult;
                invoke-interface {p1, v0}, Lcom/android/billingclient/api/BillingClientStateListener;->d(Lcom/android/billingclient/api/BillingResult;)V
                :cond_null
                return-void
            """,
        )
        patched++

        // 3. isReady returns false
        BillingClientImplIsReadyFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
        patched++

        // 4. isFeatureSupported returns BILLING_UNAVAILABLE
        BillingClientImplIsFeatureSupportedFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/android/billingclient/api/zzdb;->b:Lcom/android/billingclient/api/BillingResult;
                return-object v0
            """,
        )
        patched++

        // 5. queryPurchasesAsync returns BILLING_UNAVAILABLE with emptyList
        BillingClientImplQueryPurchasesFingerprint.method.addInstructions(
            0,
            """
                if-eqz p2, :cond_null
                sget-object v0, Lcom/android/billingclient/api/zzdb;->b:Lcom/android/billingclient/api/BillingResult;
                invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                move-result-object v1
                invoke-interface {p2, v0, v1}, Lcom/android/billingclient/api/PurchasesResponseListener;->e(Lcom/android/billingclient/api/BillingResult;Ljava/util/List;)V
                :cond_null
                return-void
            """,
        )
        patched++

        // 6. GoogleBillingService.isSupportGooglePay returns false
        GoogleBillingServiceIsSupportGooglePayFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
        patched++

        // 7. GoogleIapExternalServiceProvider.isSupportGooglePay returns false
        GoogleIapExternalServiceProviderIsSupportGooglePayFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
        patched++

        println("[Neutralize Play Billing] Applied $patched hooks -> Play Billing client calls stubbed to fail closed.")
    }
}
