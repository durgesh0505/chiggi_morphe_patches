package app.chiggi.foxone.patches.pairip

import app.chiggi.foxone.patches.shared.Constants.COMPATIBILITY_FOXONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val bypassPairipLicensePatch = bytecodePatch(
    name = "Bypass PairIP license check",
    description = "No-ops com.pairip.licensecheck.LicenseClient.checkLicense so the PairIP " +
        "(Google Play Automatic Integrity Protection) startup license check is skipped and the " +
        "\"Something went wrong / check Google Play\" screen is not shown. NOTE: this only " +
        "bypasses the client-side startup check; FOX's servers may still reject a re-signed app " +
        "for login/playback.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FOXONE)

    execute {
        PairipCheckLicenseFingerprint.method.addInstructions(0, "return-void")
    }
}
