package app.chiggi.crazygames.patches.ads

import app.chiggi.crazygames.patches.shared.Constants.COMPATIBILITY_CRAZYGAMES
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

// Capacitor PluginCall.resolve() (empty success). z() is the R8-renamed resolve(). Resolving (not a
// bare return) is required so the WebView's `await AdMob.show...()` promise settles instead of
// hanging the game flow.
private val RESOLVE_EMPTY = """
    invoke-virtual {p1}, Lcom/getcapacitor/Y;->z()V
    return-void
"""

private const val EXTENSION_CLASS = "Lapp/chiggi/crazygames/extension/AdRewardPatch;"

// Auto-grant a rewarded ad WITHOUT showing it. The web finishes (credits the reward + hides the
// loading screen) on the Dismissed event, gated on the Reward event having fired first, so we pass
// both and the extension fires them in order, then resolves. p0 = AdMob plugin, p1 = PluginCall,
// v0 = reward event, v1 = dismissed event.
private fun grantReward(rewardEvent: String, dismissEvent: String) = """
    const-string v0, "$rewardEvent"
    const-string v1, "$dismissEvent"
    invoke-static {p0, p1, v0, v1}, $EXTENSION_CLASS->grantReward(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V
    return-void
"""

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes native ads (AdMob and its Pangle/Audience Network mediation) by " +
        "neutering the Capacitor AdMob plugin (com.getcapacitor.community.admob.AdMob): " +
        "interstitial and banner shows resolve immediately with nothing displayed, and rewarded " +
        "ads auto-grant their reward without playing a video. Note: in-page web video ads served " +
        "remotely inside the WebView (from crazygames.com) are not part of the app bytecode and " +
        "cannot be removed here.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CRAZYGAMES)
    extendWith("extensions/extension.mpe")

    execute {
        // Interstitial + banner + rewarded-prepare -> resolve empty (no ad, no hang).
        listOf(
            ShowInterstitialFingerprint,
            PrepareInterstitialFingerprint,
            ShowBannerFingerprint,
            ResumeBannerFingerprint,
            PrepareRewardVideoAdFingerprint,
            PrepareRewardInterstitialAdFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, RESOLVE_EMPTY)
        }

        // Rewarded shows -> auto-grant the reward without showing the ad.
        ShowRewardVideoAdFingerprint.method.addInstructions(
            0,
            grantReward("onRewardedVideoAdReward", "onRewardedVideoAdDismissed"),
        )
        ShowRewardInterstitialAdFingerprint.method.addInstructions(
            0,
            grantReward("onRewardedInterstitialAdReward", "onRewardedInterstitialAdDismissed"),
        )
    }
}
