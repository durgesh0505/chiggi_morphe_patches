package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val CUTSAME_VM = "Lcom/vega/libcutsame/viewmodel/CutSamePurchaseViewModel;"
private object CutSameX6 : Fingerprint(definingClass = CUTSAME_VM, name = "x6", returnType = "Z", parameters = listOf())
private object CutSameA6 : Fingerprint(definingClass = CUTSAME_VM, name = "A6", returnType = "Z", parameters = listOf())
private object CutSameC6 : Fingerprint(definingClass = CUTSAME_VM, name = "C6", returnType = "Z", parameters = listOf())
private object TemplateBreakDomainE : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/domain/TemplateBreakDomain;",
    name = "e",
    returnType = "Z",
    parameters = listOf("Lcom/vega/container/session/core/ISession;", "Ljava/lang/String;"),
)
private object LoginCheckerE : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/checker/LoginChecker;",
    name = "e",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lcom/vega/subscription/core/checker/CheckerContext;", "Z", "Lkotlin/coroutines/Continuation;"),
)

@Suppress("unused")
val capcutUnlockPremiumTemplatesPatch = bytecodePatch(
    name = "Unlock Premium Templates",
    description = "Unlocks CapCut 20.0.0 premium template gates and removes the logged-out template lock. Ported from kveld9/kveld-extra-morphe-patches (GPLv3), with corrected 20.0.0 R8 mappings.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    dependsOn(capcutUnlockPremiumPatch)
    execute {
        listOf(CutSameX6, CutSameA6).forEach {
            it.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        listOf(CutSameC6, TemplateBreakDomainE).forEach {
            it.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        LoginCheckerE.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/vega/subscription/core/checker/CheckResult;
                const/4 v1, 0x0
                const/4 v2, 0x0
                const/4 v3, 0x0
                invoke-direct {v0, v1, v2, v2, v3}, Lcom/vega/subscription/core/checker/CheckResult;-><init>(ILjava/lang/String;Ljava/util/Map;Z)V
                return-object v0
            """,
        )
    }
}
