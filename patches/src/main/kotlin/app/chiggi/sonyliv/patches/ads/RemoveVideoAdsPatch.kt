package app.chiggi.sonyliv.patches.ads

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeVideoAdsPatch = bytecodePatch(
    name = "Remove video ads",
    description = "Forces PlayerUtil.isAdEnable() to false so the player neither requests nor " +
        "plays ads. Removes client-side (Google IMA) ads. Server-side stitched ads on some live " +
        "content (MediaTailor/SSAI) may still appear.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // Both isAdEnable() and isAdEnable(AssetMetadata) return true when ads should play.
        // Force both to false so every caller (SLPlayerView/SLPlayerViewModel/HomeActivity/
        // WatchButtonUseCase) skips ad setup.
        listOf(IsAdEnableFingerprint, IsAdEnableForAssetFingerprint).forEach { fingerprint ->
            fingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}
