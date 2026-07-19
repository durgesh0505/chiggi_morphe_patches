package app.chiggi.commute.patches.premium

import app.chiggi.commute.patches.shared.Constants.COMPATIBILITY_COMMUTE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks the paid 'premium' upgrade (IAP com.mediocre.commute.premium) without a " +
        "purchase. The native game asks the Java billing bridge AndroidStore whether the product is " +
        "owned via isProductIdRestored() and reads the store status via getStatusAsString(); this " +
        "forces the former to true and the latter to \"2\" (STORE_SUCCEEDED), so the game treats " +
        "premium as owned. No Play purchase is made or needed. Note: the game's actual entitlement " +
        "logic is in native libcommute.so, which cannot be patched here — if it independently " +
        "re-verifies the purchase, this may not take effect.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_COMMUTE)

    execute {
        // "Is premium owned?" -> always true.
        IsProductIdRestoredFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Store operation status -> "2" (STORE_SUCCEEDED) so the native flow trusts the check above.
        GetStatusAsStringFingerprint.method.addInstructions(0, """
            const-string v0, "2"
            return-object v0
        """)
    }
}
