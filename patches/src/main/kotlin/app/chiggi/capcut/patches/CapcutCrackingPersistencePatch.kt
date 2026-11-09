package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags

private object CrackingPersist : Fingerprint(
    definingClass = "Lcom/vega/launcher/network/interceptors/CrackingInterceptor;",
    name = "c",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("I"),
    strings = listOf("isCracking", "crackCode"),
)
@Suppress("unused")
val capcutCrackingPersistencePatch = bytecodePatch(
    name = "Do Not Persist Cracking Verdict",
    description = "Stops CapCut from persisting server cracking verdicts in the local crackingInfo preferences. Automatically included by Bypass Signature Check.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute { CrackingPersist.method.addInstructions(0, "return-void") }
}
