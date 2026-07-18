package app.chiggi.hotstar.patches.ad

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

// AdMetadata flags for a clean stream: disablePreRoll=1, disableMidRoll=1, hasPreRoll=0,
// hasPlayablePreRoll=0, enableMidRollLoad=0, and ssaiTag="" (so the client requests the
// non-ad-stitched SSAI variant — this is what removes mid-roll / live-match ads).
private val CLEAN_AD_METADATA = """
    const/4 p1, 0x1
    const/4 p2, 0x1
    const/4 p3, 0x0
    const/4 p4, 0x0
    const/4 p5, 0x0
    const-string p6, ""
"""

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes pre-roll, mid-roll and live-match video ads by clearing the SSAI ad tag " +
        "in the AdMetadata constructor, so the client requests a clean, non-ad-stitched stream at " +
        "the source. This surgical approach lets the app's own ad machinery see \"no ads\" and " +
        "behave normally (no retry loops, no empty ad slots). Approach adapted from " +
        "Paresh-Maheshwari's patches (GPL-3.0).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // Surgical hook: force clean ad metadata (ssaiTag cleared, pre/mid-roll disabled) so no
        // ad-stitched stream is ever requested. Removes mid-roll / live-match video ads at the
        // source without fighting the app's ad machinery.
        AdMetadataConstructorFingerprint.method.addInstructions(0, CLEAN_AD_METADATA)

        // Hide the home-feed banner (masthead) ad render. Guarded: this only blocks the render, so a
        // small empty gap can remain where the banner was; a gap-free removal needs a feed-model
        // filter that isn't statically pinpointable without runtime UI inspection.
        runCatching {
            DisplayAdComposableFingerprint.method.addInstructions(0, "return-void")
        }
    }
}
