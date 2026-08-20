package app.chiggi.zee5.patches.ads

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove video ads",
    description = "Blocks ZEE5 video ads (pre-roll, mid-roll and the video-start slate) client-side. " +
        "The player inserts ads from an ad-tag URL supplied by MediaData (adsUrl + adTags), not from " +
        "the stream, and ad-free users already receive a null adsUrl / empty adTags — a supported " +
        "path. This forces MediaData.getAdsUrl() to null and getAdTags() to an empty list so no ad " +
        "break is ever built, and forces FetchPlaybackUrlUseCase.toPreRollDaiSlateMedia() to null so " +
        "no pre-roll slate is prepended. Content goes straight to play — fewer ad requests, so it is " +
        "lighter, not laggy. Server-DAI mid-roll (if a title is DAI-only) is not affected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        // No ad-tag URL -> the player builds no AdConfig (the ad-free code path).
        GetAdsUrlFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // Empty ad-tag list -> no ad breaks (safe empty List, callers just iterate nothing).
        GetAdTagsFingerprint.method.addInstructions(0, """
            invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
            move-result-object v0
            return-object v0
        """)

        // No pre-roll DAI slate -> content plays first, no video-start ad.
        ToPreRollDaiSlateFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)
    }
}
