package app.chiggi.bighunter.patches.license

import app.chiggi.bighunter.patches.shared.Constants.COMPATIBILITY_BIG_HUNTER
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val bypassLicenseCheckPatch = bytecodePatch(
    name = "Bypass license check",
    description = "Stops the PairIP (Google Play) license check from redirecting a re-signed build to " +
        "the Play Store (\"get this game from Play\") and killing the app. Forces " +
        "LicenseClient.initializeLicenseCheck() to a no-op (no Play bind, no paywall) and neuters " +
        "startPaywallActivity()/startErrorDialogActivity() as defense. Nothing outside com.pairip.* " +
        "reads the license state, so gameplay is unaffected. Required for the re-signed offline build " +
        "to launch at all.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BIG_HUNTER)

    execute {
        InitializeLicenseCheckFingerprint.method.addInstructions(0, "return-void")
        StartPaywallActivityFingerprint.method.addInstructions(0, "return-void")
        StartErrorDialogActivityFingerprint.method.addInstructions(0, "return-void")
    }
}
