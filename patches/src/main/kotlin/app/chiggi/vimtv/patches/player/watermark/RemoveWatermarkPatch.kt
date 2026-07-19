package app.chiggi.vimtv.patches.player.watermark

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeWatermarkPatch = bytecodePatch(
    name = "Remove player watermark",
    description = "Removes the per-content watermark/logo overlaid on the video (the one that " +
        "changes depending on what you play). The player only shows the watermark ImageView when " +
        "Configs.getWaterMarkInfoOnPlayer() returns non-empty JSON; forcing that getter to null " +
        "makes the player's own guard hide the overlay. Client render only, so this is fully " +
        "removable (it is not baked into the stream).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    execute {
        // No watermark info -> ExoPlayerFragment's null guard sets the watermark ImageView GONE.
        GetWaterMarkInfoOnPlayerFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)
    }
}
