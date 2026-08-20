package app.chiggi.zee5.patches.misc.popups

import app.morphe.patcher.Fingerprint

/**
 * Two nuisance popups gated by simple boolean getters on HomeActivityViewModel:
 *  - canLaunchAppRating() decides whether the "rate this app" dialog may show.
 *  - toShowBuyPlanPopup() decides whether the subscription upsell popup shows on home.
 * Forcing both to false suppresses the popups. Names are not obfuscated.
 */
internal object CanLaunchAppRatingFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/home/presentation/HomeActivityViewModel;",
    name = "canLaunchAppRating",
    returnType = "Z",
)

internal object ToShowBuyPlanPopupFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/home/presentation/HomeActivityViewModel;",
    name = "toShowBuyPlanPopup",
    returnType = "Z",
)
