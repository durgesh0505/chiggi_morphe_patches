package app.chiggi.hotstar.patches.playback

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val forceHdr10Patch = bytecodePatch(
    name = "Force HDR10",
    description = "Forces HDR10 / HDR10+ / Dolby Vision playback by bypassing the capability/" +
        "blacklist check. Off by default: forcing HDR on a panel that cannot render it can wash out " +
        "or break playback. Approach adapted from Paresh-Maheshwari's patches (GPL-3.0).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // Force Dolby Vision / HDR support -> true.
        IsDolbyVisionSupportedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
