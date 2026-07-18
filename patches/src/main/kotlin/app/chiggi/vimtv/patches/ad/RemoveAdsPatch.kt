package app.chiggi.vimtv.patches.ad

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes pre-roll, mid-roll and SSAI video ads. The player builds its IMA/SSAI " +
        "ad-tag URL from AdUrlResponse.getAdUrlTypes(); forcing that to null (and the " +
        "TorcAiAdConfig.isAdEnabled() gate to false) means no ad tag is ever loaded, so no ad plays.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    execute {
        // No ad-tag list -> the player's ad-load paths (guarded by null/size checks) all skip.
        GetAdUrlTypesFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // Higher ad-enable gate -> false.
        IsAdEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
