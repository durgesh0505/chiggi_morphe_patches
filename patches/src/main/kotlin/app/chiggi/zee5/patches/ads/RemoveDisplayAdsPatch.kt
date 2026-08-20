package app.chiggi.zee5.patches.ads

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeDisplayAdsPatch = bytecodePatch(
    name = "Remove display ads",
    description = "Removes ZEE5 non-video display ads: the home-screen native masthead banner " +
        "(HomeActivityViewModel.getNeedToShowNativeMastheadAd() forced false, so it never loads) and " +
        "the on-pause display overlay (FetchPlaybackUrlUseCase.toPauseAds() forced null). No extra ad " +
        "requests are made, so this makes the UI lighter rather than laggier.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        // Home masthead banner never requested.
        NeedToShowMastheadAdFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // No on-pause display ad built.
        ToPauseAdsFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)
    }
}
