package app.chiggi.zee5.patches.player.buffer

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val fasterStartPatch = bytecodePatch(
    name = "Faster video start",
    description = "Lowers the ExoPlayer start buffer (bufferForPlayback) to 1500ms by forcing " +
        "DefaultPlayerConfig.defaultBuffersForPlaybackMs() to return 1500 instead of the server value " +
        "(~2500ms). Playback starts after less initial buffering, so the first frame appears sooner. " +
        "The player still fills the full min/max buffer in the background; only the start threshold " +
        "drops. On a weak network or slow CPU this slightly raises the chance of an early re-buffer. " +
        "Does not affect the DRM-license/manifest part of startup.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        // 1500 = 0x5DC. Stays well under min buffer, so DefaultLoadControl.Builder stays valid.
        DefaultBuffersForPlaybackMsFingerprint.method.addInstructions(0, """
            const/16 v0, 0x5dc
            return v0
        """)
    }
}
