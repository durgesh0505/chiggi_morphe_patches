package app.chiggi.sonyliv.patches.misc.analytics

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableAppsFlyerPatch = bytecodePatch(
    name = "Disable AppsFlyer tracking",
    description = "Disables AppsFlyer attribution and event tracking. Other SDKs " +
        "(CleverTap, Firebase Analytics/Crashlytics) are not affected by this patch.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // isAppsFlyerSupported() returns true and guards every AppsFlyer init/track call.
        // Force it to false so AppsFlyer is never initialised and no events are sent.
        IsAppsFlyerSupportedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
