package app.chiggi.bighunter.patches.ads

import app.chiggi.bighunter.patches.shared.Constants.COMPATIBILITY_BIG_HUNTER
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Suppresses AppLovin MAX / AdMob / Unity ads. Interstitial show() becomes a no-op " +
        "(only releases the native processing lock via stopProcess). Rewarded showWithReward() skips " +
        "the ad but still fires the native rewardCallback() so reward-gated features (revive, bonus " +
        "coins, etc.) keep working, then releases the lock. getIsReadyReward() is forced true so the " +
        "reward button stays available without a preloaded ad. Unity's reward is not wired to a Java " +
        "callback, so its showWithReward only skips the ad.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BIG_HUNTER)

    execute {
        // Interstitials -> no-op (release native lock only).
        AppLovinShowFingerprint.method.addInstructions(0, """
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsAppLovin;->stopProcess()V
            return-void
        """)
        AdmobShowFingerprint.method.addInstructions(0, """
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsAdmob;->stopProcess()V
            return-void
        """)
        UnityShowFingerprint.method.addInstructions(0, """
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsUnity;->stopProcess()V
            return-void
        """)

        // Rewarded -> skip ad, grant reward (AppLovin + AdMob), release lock.
        AppLovinShowRewardFingerprint.method.addInstructions(0, """
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsAppLovin;->rewardCallback()V
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsAppLovin;->stopProcess()V
            return-void
        """)
        AdmobShowRewardFingerprint.method.addInstructions(0, """
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsAdmob;->rewardCallback()V
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsAdmob;->stopProcess()V
            return-void
        """)
        // Unity reward has no Java rewardCallback call site -> only skip the ad.
        UnityShowRewardFingerprint.method.addInstructions(0, """
            invoke-static {}, LkakarodJavaLibs/data/KKJAdsUnity;->stopProcess()V
            return-void
        """)

        // Keep the reward button visible without a real preloaded ad.
        AppLovinIsReadyRewardFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        AdmobIsReadyRewardFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        UnityIsReadyRewardFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
