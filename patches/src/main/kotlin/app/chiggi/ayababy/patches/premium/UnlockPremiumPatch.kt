package app.chiggi.ayababy.patches.premium

import app.chiggi.ayababy.patches.shared.Constants.COMPATIBILITY_AYABABY
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private val RETURN_TRUE = """
    const/4 v0, 0x1
    return v0
"""

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks AYA Baby AI premium without a purchase by forcing the Apphud SDK " +
        "entitlement checks Apphud.hasPremiumAccess() and hasActiveSubscription() to return true. " +
        "The app gates premium features on these, so the app treats the user as subscribed. Because " +
        "it uses an ads-or-premium model, unlocking premium also stops the Appodeal/AdMob ads. No " +
        "Google Play purchase is made or needed.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_AYABABY)

    execute {
        HasPremiumAccessFingerprint.method.addInstructions(0, RETURN_TRUE)
        HasActiveSubscriptionFingerprint.method.addInstructions(0, RETURN_TRUE)
    }
}
