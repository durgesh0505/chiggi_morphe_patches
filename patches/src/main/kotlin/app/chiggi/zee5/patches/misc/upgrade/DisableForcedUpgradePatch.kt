package app.chiggi.zee5.patches.misc.upgrade

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableForcedUpgradePatch = bytecodePatch(
    name = "Disable forced upgrade",
    description = "Stops ZEE5 from forcing an app update. BlockerScreenInfo.isBlocked() gates the " +
        "block/upgrade wall; forcing it false lets the current version keep running. This is the only " +
        "hard client block screen in the app (there is no geo/VPN/tamper block).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        IsBlockedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
