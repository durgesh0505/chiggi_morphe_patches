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

/**
 * The device-attestation blob data class (LKg/b;) sent to Hotstar's server. Its 34-String
 * constructor carries the device fingerprint plus 8 security flags in this order (param index):
 *  25 IsEmulator, 26 IsRooted, 27 IsTampered, 28 IsProxy, 29 isVpnActive,
 *  30 isSuspiciousFileExists, 31 isPortsOpen, 32 isDebuggerEnabled.
 * IsTampered comes from the native tamper checker (libtoolChecker), NOT the securityLib lazy checks,
 * so it was reported honestly as "true" on a re-signed build — which is what the server flags
 * (error NM-4290, "suspicious activity ... locked 24 hours"). Forcing all 8 flags to "false" at the
 * constructor makes the whole attestation report a clean device at one chokepoint.
 */
internal object AttestationBlobConstructorFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    parameters = List(34) { "Ljava/lang/String;" },
    definingClass = "LKg/b;",
)
