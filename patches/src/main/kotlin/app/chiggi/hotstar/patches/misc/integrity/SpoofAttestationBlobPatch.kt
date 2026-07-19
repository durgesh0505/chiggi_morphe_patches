package app.chiggi.hotstar.patches.misc.integrity

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

// Overwrite the 8 security-flag String params (indices 25..32) with "false" at the very top of the
// attestation-blob constructor, before they are stored into fields. This makes the blob the app
// sends to Hotstar report IsEmulator/IsRooted/IsTampered/IsProxy/isVpnActive/isSuspiciousFileExists/
// isPortsOpen/isDebuggerEnabled all false — a clean device — regardless of what the checks (incl. the
// native tamper checker feeding IsTampered) computed.
private val CLEAN_ATTESTATION = """
    const-string p25, "false"
    const-string p26, "false"
    const-string p27, "false"
    const-string p28, "false"
    const-string p29, "false"
    const-string p30, "false"
    const-string p31, "false"
    const-string p32, "false"
	invoke-static {}, Lapp/chiggi/hotstar/extension/DeviceIdSpoof;->gaid()Ljava/lang/String;
    move-result-object p33
    invoke-static {}, Lapp/chiggi/hotstar/extension/DeviceIdSpoof;->widevineId()Ljava/lang/String;
    move-result-object p34
"""

@Suppress("unused")
val spoofAttestationBlobPatch = bytecodePatch(
    name = "Spoof device attestation",
    description = "Forces the device-attestation blob JioHotstar sends to the server to report a " +
        "clean device (IsTampered/IsRooted/IsProxy/isVpnActive/IsEmulator/suspicious-file/open-" +
        "ports/debugger all false). The re-signed build otherwise sends IsTampered=true, which the " +
        "server flags as \"suspicious activity\" and locks the account for 24 hours (error NM-4290). " +
        "WARNING: this reduces one client-sent signal but does NOT make the account safe — the " +
        "server still sees the VPN exit IP, and an already-flagged account stays on Hotstar's radar. " +
		"It also randomizes the reported GAID and Widevine device id so a device Hotstar has " +
        "blocklisted presents as fresh (playback is unaffected — the DRM Widevine is separate). " +
        "Do not run this on an account you cannot afford to lose.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)
	extendWith("extensions/extension.mpe")

    execute {
        AttestationBlobConstructorFingerprint.method.addInstructions(0, CLEAN_ATTESTATION)
    }
}
