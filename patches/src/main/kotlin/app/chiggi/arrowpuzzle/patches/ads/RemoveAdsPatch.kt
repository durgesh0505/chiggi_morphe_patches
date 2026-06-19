package app.chiggi.arrowpuzzle.patches.ads

import app.chiggi.arrowpuzzle.patches.shared.Constants.COMPATIBILITY_ARROWPUZZLE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private val RETURN_FALSE = """
    const/4 v0, 0x0
    return v0
"""

private val RETURN_ZERO = """
    const/4 v0, 0x0
    return v0
"""

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes ALL ads (banner, interstitial and rewarded) by neutering the Unity<->" +
        "Android ad facade (com.easybrain.ads.unity.AdsPlugin): availability/show booleans return " +
        "false, banner shows are no-ops and banner height is 0. The game treats every ad type as " +
        "never ready. Note: rewarded ads are also removed, so watch-to-earn rewards no longer work.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ARROWPUZZLE)

    execute {
        // Boolean availability/show gates -> always "no ad available / show failed".
        listOf(
            IsInterstitialCachedFingerprint,
            IsInterstitialReadyFingerprint,
            ShowInterstitialFingerprint,
            IsRewardedCachedFingerprint,
            ShowRewardedFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, RETURN_FALSE)
        }

        // Banner shows -> no-op; enable -> no-op (so the banner flow never starts).
        listOf(
            ShowBannerFingerprint,
            ShowBannerWithPositionFingerprint,
            EnableBannerFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }

        // Banner height -> 0 so Unity never reserves on-screen space for a banner.
        listOf(
            GetBannerHeightFingerprint,
            GetBannerHeightWithArgFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, RETURN_ZERO)
        }
    }
}
