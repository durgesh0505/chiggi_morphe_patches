package app.chiggi.hotstar.patches.misc.integrity

import app.morphe.patcher.Fingerprint

/**
 * com.hotstar.securityLib.* — Hotstar's device-integrity self-report engine. Each check is a lazy
 * lambda returning a boxed Boolean that feeds the attestation blob the app sends to the server
 * (IsRooted / IsProxy / isVpnActive / isDebuggerEnabled / isPortsOpen / isSuspiciousFileExists).
 * Forcing them false makes the client report a clean device. Class names embed the field name
 * (vpnEnabled/proxyEnabled/...), so they are stable anchors; pinned to 26.06.22.3.
 */
internal object VpnEnabledFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    definingClass = "Lcom/hotstar/securityLib/AppSuiteType\$vpnEnabled\$2;",
)

internal object ProxyEnabledFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    definingClass = "Lcom/hotstar/securityLib/AppSuiteType\$proxyEnabled\$2;",
)

internal object DebuggerAttachedFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    definingClass = "Lcom/hotstar/securityLib/AppSuiteType\$debuggerAttached\$2;",
)

internal object FileExistsFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    definingClass = "Lcom/hotstar/securityLib/AppSuiteType\$fileExists\$2;",
)

internal object PortOpenedFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    definingClass = "Lcom/hotstar/securityLib/AppSuiteType\$portOpened\$2;",
)

/**
 * Root check is a suspend function (coroutine), so its body is invokeSuspend(Object)Object; returning
 * Boolean.FALSE at the top completes it as "not rooted".
 */
internal object IsRootedFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    definingClass = "Lcom/hotstar/securityLib/AppSuiteImpl\$getIsRooted\$1;",
)
