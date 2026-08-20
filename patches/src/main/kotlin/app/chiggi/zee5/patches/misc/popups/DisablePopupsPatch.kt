package app.chiggi.zee5.patches.misc.popups

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disablePopupsPatch = bytecodePatch(
    name = "Disable nuisance popups",
    description = "Suppresses the ZEE5 app-rating prompt and the subscription 'Buy Plan' upsell popup " +
        "on the home screen by forcing HomeActivityViewModel.canLaunchAppRating() and " +
        "toShowBuyPlanPopup() to false. Both are plain boolean gates, so this only stops the dialogs " +
        "from being shown; nothing else changes.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        CanLaunchAppRatingFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        ToShowBuyPlanPopupFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
