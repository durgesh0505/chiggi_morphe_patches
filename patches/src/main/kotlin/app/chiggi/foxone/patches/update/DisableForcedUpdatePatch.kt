package app.chiggi.foxone.patches.update

import app.chiggi.foxone.patches.shared.Constants.COMPATIBILITY_FOXONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableForcedUpdatePatch = bytecodePatch(
    name = "Disable forced update",
    description = "Disables the forced-update / kill-switch screen by forcing the remote " +
        "AppUpdateConfig.enabled flag to false.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FOXONE)

    execute {
        AppUpdateConfigEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
