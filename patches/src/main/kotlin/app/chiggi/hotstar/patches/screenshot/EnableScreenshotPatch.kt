package app.chiggi.hotstar.patches.screenshot

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enableScreenshotPatch = bytecodePatch(
    name = "Enable screenshots",
    description = "Removes screenshot and screen-recording restrictions on the app UI (the DRM " +
        "video itself stays protected by Widevine). Approach adapted from Paresh-Maheshwari's " +
        "patches (GPL-3.0).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // ABConfig.getUseSecureView() -> false (disables SurfaceView.setSecure).
        UseSecureViewFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Compose FLAG_SECURE helper -> return early (skips Window.addFlags(0x2000)). Guard: the
        // compose helper is obfuscated and may shift.
        runCatching {
            ComposeFlagSecureFingerprint.method.addInstructions(0, "return-void")
        }
    }
}
