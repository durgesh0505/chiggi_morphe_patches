package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object VersionUpdateGate : Fingerprint(definingClass = "Lcom/vega/main/update/VersionUpdateService;", name = "a", returnType = "Z", parameters = listOf())
@Suppress("unused")
val capcutDisableForcedUpdatePatch = bytecodePatch(
    name = "Disable Forced Update",
    description = "Disables CapCut's server-driven update popup gate.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute { VersionUpdateGate.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
}
