package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object CrackingPersist : Fingerprint(
    definingClass = "Lcom/vega/launcher/network/interceptors/CrackingInterceptor;", name = "c", returnType = "V", parameters = listOf("I"),
)
@Suppress("unused")
val capcutCrackingPersistencePatch = bytecodePatch(
    name = "Do Not Persist Cracking Verdict",
    description = "Optional: stops local crackingInfo persistence. Off by default because server verdict echoes should normally remain intact.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute { CrackingPersist.method.addInstructions(0, "return-void") }
}
