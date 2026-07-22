package app.chiggi.cryanalyzer.patches.premium

import app.chiggi.cryanalyzer.patches.shared.Constants.COMPATIBILITY_CRYANALYZER
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks Cry Analyzer premium (the monthly / half-year subscription) without a " +
        "purchase by forcing BillingClientWrapper.isPurchased(...) to return true, so every " +
        "subscription/ownership check reports the product as owned. In this app's freemium model " +
        "that also removes the ads gated behind premium. No Google Play purchase is made or needed.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CRYANALYZER)

    execute {
        IsPurchasedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
