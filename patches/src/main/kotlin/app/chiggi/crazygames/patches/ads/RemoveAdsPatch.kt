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

// Complete a modal ad (interstitial / rewarded) WITHOUT showing it. The web keeps a loading overlay
// up until the terminal "Dismissed" event finishes the ad, and that event only counts if the first
// lifecycle event (interstitial Showed / rewarded Reward) fired before it. So the extension fires
// both in order, then resolves. p0 = AdMob plugin, p1 = PluginCall, v0 = first event, v1 = dismissed.
private fun fireAdEvents(firstEvent: String, dismissEvent: String) = """
    const-string v0, "$firstEvent"
    const-string v1, "$dismissEvent"
    invoke-static {p0, p1, v0, v1}, $EXTENSION_CLASS->fireAdEvents(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V
    return-void
"""

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes native ads (AdMob and its Pangle/Audience Network mediation) by " +
        "neutering the Capacitor AdMob plugin (com.getcapacitor.community.admob.AdMob): no video " +
        "is shown for interstitial or rewarded ads — instead the show methods fire the ad's " +
        "lifecycle events so the game's loading overlay dismisses and rewarded ads still credit " +
        "their reward; banners never display. Note: in-page web video ads served remotely inside " +
        "the WebView (from crazygames.com) are not part of the app bytecode and cannot be removed " +
        "here.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CRAZYGAMES)
    extendWith("extensions/extension.mpe")

    execute {
        // Prepares + banner shows -> resolve empty. Prepares only await the promise (no Loaded-event
        // gate), and banners are non-modal (no loading overlay), so an empty resolve is enough.
        listOf(
            PrepareInterstitialFingerprint,
            ShowBannerFingerprint,
            ResumeBannerFingerprint,
            PrepareRewardVideoAdFingerprint,
            PrepareRewardInterstitialAdFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, RESOLVE_EMPTY)
        }

        // Modal shows -> fire the lifecycle events the web waits on, so the loading overlay
        // dismisses (and rewarded ads credit), without playing a video.
        // Interstitial events are named "interstitialAd*"; rewarded events are named "onRewarded*".
        ShowInterstitialFingerprint.method.addInstructions(
            0,
            fireAdEvents("interstitialAdShowed", "interstitialAdDismissed"),
        )
        ShowRewardVideoAdFingerprint.method.addInstructions(
            0,
            fireAdEvents("onRewardedVideoAdReward", "onRewardedVideoAdDismissed"),
        )
        ShowRewardInterstitialAdFingerprint.method.addInstructions(
            0,
            fireAdEvents("onRewardedInterstitialAdReward", "onRewardedInterstitialAdDismissed"),
        )
    }
}
