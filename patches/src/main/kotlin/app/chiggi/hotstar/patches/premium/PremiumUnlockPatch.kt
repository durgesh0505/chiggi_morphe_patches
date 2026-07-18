package app.chiggi.hotstar.patches.premium

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val premiumUnlockPatch = bytecodePatch(
    name = "Premium unlock",
    description = "Unlocks premium UI state: shows the subscribed badge, enables the download " +
        "button for all content and suppresses subscription nudges during playback. This is a UI " +
        "unlock only — it does not grant access to DRM content you are not entitled to. Approach " +
        "adapted from Paresh-Maheshwari's patches (GPL-3.0).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // MyPageData.getIsSubscribed() -> true (subscribed UI state).
        IsSubscribedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // DownloadInfo.getIsPremium() -> false (download button for all content).
        IsPremiumFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Suppress the subscription nudge during playback (guard: obfuscated handler).
        runCatching {
            SubscriptionNudgeFingerprint.method.addInstructions(0, "return-void")
        }
    }
}
