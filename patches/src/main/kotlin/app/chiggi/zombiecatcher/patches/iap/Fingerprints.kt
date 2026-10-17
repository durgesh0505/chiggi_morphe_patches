package app.chiggi.zombiecatcher.patches.iap

import app.morphe.patcher.Fingerprint

private const val IN_APP_SERVICE = "Lfi/twomenandadog/zombiecatchers/InAppServiceImpl;"

internal object InitInAppServiceFingerprint : Fingerprint(
    definingClass = IN_APP_SERVICE,
    name = "initInAppService",
    returnType = "V",
    parameters = listOf("Lfi/twomenandadog/zombiecatchers/ZCActivity;"),
)

internal object ConnectStoreFingerprint : Fingerprint(
    definingClass = IN_APP_SERVICE,
    name = "connectStore",
    returnType = "V",
    parameters = listOf("J"),
)

internal object QueryInventoryFingerprint : Fingerprint(
    definingClass = IN_APP_SERVICE,
    name = "queryInventory",
    returnType = "V",
    parameters = listOf("[Ljava/lang/String;", "J"),
)

internal object PurchaseFingerprint : Fingerprint(
    definingClass = IN_APP_SERVICE,
    name = "purchase",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "J"),
)
