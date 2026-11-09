package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private fun boolGate(clazz: String, name: String) = Fingerprint(definingClass = clazz, name = name, returnType = "Z", parameters = listOf())
private val ColdH = boolGate("Lcom/vega/main/utils/ColdStartLoginHelper;", "h")
private val ColdI = boolGate("Lcom/vega/main/utils/ColdStartLoginHelper;", "i")
private val ExportB = boolGate("Lcom/vega/export/loginreward/ExportLoginRewardManager;", "b")
private val ExportD = boolGate("Lcom/vega/export/loginreward/ExportLoginRewardManager;", "d")
private object DoLogin : Fingerprint(
    definingClass = "Lcom/lemon/LoginUtilKt;", name = "doLogin", returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Landroid/os/Bundle;", "Z", "Lkotlin/jvm/functions/Function1;"),
)

@Suppress("unused")
val capcutRemoveLoginPromptsPatch = bytecodePatch(
    name = "Remove Login Prompts",
    description = "Suppresses cold-start and export login prompts. Cloud, AI and social server features still require a real session.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute {
        listOf(ColdH, ColdI, ExportB, ExportD).forEach { it.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
    }
}

@Suppress("unused")
val capcutBlockLoginScreensPatch = bytecodePatch(
    name = "Block All Login Screens",
    description = "Stops the central login action and reports cancellation to its callback. Server-session features become unavailable instead of opening login.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute {
        DoLogin.method.addInstructions(0, """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            invoke-interface {p3, v0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
            return-void
        """)
    }
}
