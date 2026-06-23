package app.chiggi.crazygames.patches.misc.integrity

import app.chiggi.crazygames.patches.shared.Constants.COMPATIBILITY_CRAZYGAMES
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableLicenseCheckPatch = bytecodePatch(
    name = "Bypass Play license check",
    description = "Stops Google Play's Automatic Integrity Protection (PairIP) license check that, " +
        "on a re-signed/sideloaded build, redirects the app to the Play Store (\"get it on Google " +
        "Play\"). Neuters both entry points of com.pairip.licensecheck.LicenseClient " +
        "(checkLicense / initializeLicenseCheck) so the check never runs. Required for the patched " +
        "app to open at all.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CRAZYGAMES)

    execute {
        listOf(
            CheckLicenseFingerprint,
            InitializeLicenseCheckFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }
    }
}
