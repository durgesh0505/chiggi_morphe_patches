package app.chiggi.hotstar.patches.misc.integrity

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

// Each security check returns a boxed Boolean; force Boolean.FALSE so the device reports clean.
private val RETURN_FALSE = """
    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
    return-object v0
"""

@Suppress("unused")
val spoofSecurityStatePatch = bytecodePatch(
    name = "Spoof device-integrity self-report",
    description = "Forces JioHotstar's security-suite checks (VPN, proxy, root, debugger, open " +
        "ports, suspicious files) to report false, so the device-integrity attestation the app " +
        "sends to the server looks clean. Helps rooted/modded devices and complements the VPN " +
        "bypass. Note: the attestation is also server-validated (signed nonce) and the server sees " +
        "the connection IP, so this is not guaranteed to defeat a server-side check on its own.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        listOf(
            VpnEnabledFingerprint,
            ProxyEnabledFingerprint,
            DebuggerAttachedFingerprint,
            FileExistsFingerprint,
            PortOpenedFingerprint,
            IsRootedFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, RETURN_FALSE)
        }
    }
}
