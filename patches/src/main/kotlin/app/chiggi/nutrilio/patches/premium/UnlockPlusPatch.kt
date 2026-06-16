package app.chiggi.nutrilio.patches.premium

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks Nutrilio PLUS+ by forcing the premium gate (PremiumModule.t2()) to " +
        "return true. Every premium feature (custom colors, all charts, app lock, all tracking " +
        "options) and the paywall treat the user as a subscriber. Client-side only; does not grant " +
        "a real Google Play subscription.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

    execute {
        // Master premium flag getter -> always report Plus active.
        PremiumModuleIsPlusActiveFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
