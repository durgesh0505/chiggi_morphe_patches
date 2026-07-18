package app.chiggi.hotstar.patches.mirroring

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enableScreenMirroringPatch = bytecodePatch(
    name = "Enable screen mirroring",
    description = "Allows playback to continue while screen mirroring or an external HDMI display " +
        "is connected, instead of showing the multiple-display block. Approach adapted from " +
        "Paresh-Maheshwari's patches (GPL-3.0).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // NOP the multiple-display detection so the "multiple screens detected" block never fires.
        // Guarded: the check is structured differently across versions; if the fingerprint does not
        // resolve on this build the patch is a no-op rather than aborting the whole apply.
        runCatching {
            MultipleDisplayCheckFingerprint.method.addInstructions(0, "return-void")
        }
    }
}
