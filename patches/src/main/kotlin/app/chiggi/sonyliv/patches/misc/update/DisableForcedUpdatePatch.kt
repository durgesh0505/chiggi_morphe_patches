package app.chiggi.sonyliv.patches.misc.update

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableForcedUpdatePatch = bytecodePatch(
    name = "Disable forced update",
    description = "Removes the forced ('immediate') and optional ('flexi') 'update available' " +
        "popup shown on the home screen.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // getAppUpgradeData() builds the popup Bundle, or returns null when no update is required.
        // Force it to always return null so neither the immediate nor flexi update popup is shown.
        AppUpgradeDataFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        )
    }
}
