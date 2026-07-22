package app.chiggi.cryanalyzer.patches.premium

import app.chiggi.cryanalyzer.patches.shared.Constants.COMPATIBILITY_CRYANALYZER
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks Cry Analyzer premium and removes all usage limits without a purchase. " +
        "Forces BillingClientWrapper.isPurchased(...) true (every ownership check reports owned) and " +
        "UserData.getRestrictionRelease() true (the master 'restrictions removed' flag). Together " +
        "this gives unlimited analyses with no free-count / rewarded-ad requirement, no ads, and no " +
        "free-analysis reminder nag. No Google Play purchase is made or needed.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CRYANALYZER)

    execute {
        // "Is the product purchased?" -> always true.
        IsPurchasedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Master "restrictions removed" flag -> always TRUE: unlimited analyses, no ads, no nag.
        GetRestrictionReleaseFingerprint.method.addInstructions(0, """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
        """)
    }
}
