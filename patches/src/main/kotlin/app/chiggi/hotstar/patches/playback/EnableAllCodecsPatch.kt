package app.chiggi.hotstar.patches.playback

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enableAllCodecsPatch = bytecodePatch(
    name = "Enable all codecs",
    description = "Forces all video codecs (H265/VP9/AV1) and 4K support on by bypassing the " +
        "capability/blacklist checks. Off by default: forcing a codec/resolution a TV cannot " +
        "actually decode can break playback. Approach adapted from Paresh-Maheshwari's patches " +
        "(GPL-3.0).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // Force 4K support -> true.
        Is4kSupportedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Force codec support checks -> true.
        IsCodecSupportedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
