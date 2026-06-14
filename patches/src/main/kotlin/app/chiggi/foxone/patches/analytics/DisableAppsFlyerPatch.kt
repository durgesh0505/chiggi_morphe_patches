package app.chiggi.foxone.patches.analytics

import app.chiggi.foxone.patches.shared.Constants.COMPATIBILITY_FOXONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableAppsFlyerPatch = bytecodePatch(
    name = "Disable AppsFlyer tracking",
    description = "Stops AppsFlyer from initialising and tracking by no-oping the wrapper that " +
        "calls AppsFlyerLib.start().",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FOXONE)

    execute {
        // The wrapper method (obfuscated name) that calls AppsFlyerLib.init()/start().
        AppsFlyerStartFingerprint.method.addInstructions(0, "return-void")
    }
}
