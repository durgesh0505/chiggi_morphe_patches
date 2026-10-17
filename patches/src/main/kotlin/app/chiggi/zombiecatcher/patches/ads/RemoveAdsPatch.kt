package app.chiggi.zombiecatcher.patches.ads

import app.chiggi.zombiecatcher.patches.shared.Constants.COMPATIBILITY_ZOMBIE_CATCHERS
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS = "Lapp/chiggi/zombiecatcher/extension/ZombieCatchersPatch;"
private val RETURN_TRUE = """
    const/4 v0, 0x1
    return v0
"""

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables AppLovin MAX initialization, banners, and real ad playback. " +
        "Interstitial requests complete immediately. Rewarded requests call the game's native " +
        "successful completion callback so reward-gated actions can still grant their reward. " +
        "Runtime confirmation is required because the final reward logic is native C++.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZOMBIE_CATCHERS)
    extendWith("extensions/extension.mpe")

    execute {
        AppLovinInitFingerprint.method.addInstructions(0, "return-void")
        AppLovinInitializedFingerprint.method.addInstructions(0, RETURN_TRUE)
        HasVideosAvailableFingerprint.method.addInstructions(0, RETURN_TRUE)

        listOf(
            LoadBannerFingerprint,
            DestroyBannerFingerprint,
            ShowBannerFingerprint,
            HideBannerFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }

        ShowInterstitialFingerprint.method.addInstructions(0, """
            invoke-static {p0}, $EXTENSION_CLASS->finishInterstitial(Ljava/lang/Object;)V
            return-void
        """)
        PlayRewardedAdFingerprint.method.addInstructions(0, """
            invoke-static {p0}, $EXTENSION_CLASS->finishRewardedAd(Ljava/lang/Object;)V
            return-void
        """)
    }
}
