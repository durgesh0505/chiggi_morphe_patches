package app.chiggi.zee5.patches.player.watermark

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeWatermarkPatch = bytecodePatch(
    name = "Remove player logo",
    description = "Removes the ZEE5/channel logo overlaid on the video player. The logo is shown via " +
        "WaterMarkViewModel's _logoVisibility LiveData; forcing updateLogoVisibility(...) to always " +
        "post false keeps the logo hidden. Client render only — this does not affect any logo baked " +
        "into a partner's video stream.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        // Overwrite the boolean argument (p1) with false at method entry, so _logoVisibility is only
        // ever set to false and WaterMarkFragment never shows the logo.
        UpdateLogoVisibilityFingerprint.method.addInstructions(0, """
            const/4 p1, 0x0
        """)
    }
}
