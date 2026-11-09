package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private fun adGate(clazz: String) = Fingerprint(definingClass = clazz, name = "i", returnType = "Z", parameters = listOf())
private val adGates = listOf(
    adGate("Lcom/vega/adapi/config/BaseAdConfigData;"),
    adGate("Lcom/vega/adapi/config/SplashAdConfigData;"),
    adGate("Lcom/vega/adapi/config/TemplateRewardAdConfigData;"),
    adGate("Lcom/vega/adapi/config/AITemplateRewardAdConfig;"),
    adGate("Lcom/vega/adapi/config/TemplateRewardDistributeAdConfigData;"),
    adGate("Lcom/vega/adapi/config/TemplateMultiAdConfig;"),
)
private object SplashAdService : Fingerprint(
    definingClass = "Lcom/vega/business/ad/impl/AdServiceImpl;", name = "h", returnType = "Z", parameters = listOf("Z", "Z"),
)
private object FreeEditExportRewardAd : Fingerprint(
    definingClass = "Lcom/vega/ad/impl/reward/FreeEditExportRewardAdKMPImpl;",
    name = "b",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Lcom/vega/adapi/constant/AdSceneTag;",
        "Landroidx/activity/ComponentActivity;",
        "Ljava/lang/String;",
        "Lorg/json/JSONObject;",
        "Lcom/vega/adapi/api/freeReward/RewardCallBack;",
        "Lkotlin/coroutines/Continuation;",
    ),
)
private object T4RewardAd : Fingerprint(
    definingClass = "Lcom/vega/ad/impl/reward/T4RewardAdViewModel;",
    name = "y6",
    returnType = "V",
    parameters = listOf(
        "Lcom/vega/adapi/constant/AdSceneTag;",
        "Landroidx/appcompat/app/AppCompatActivity;",
        "Ljava/lang/String;",
        "Lorg/json/JSONObject;",
        "Lcom/vega/adapi/api/freeReward/RewardCallBack;",
    ),
)
private object FreeCutsameAd : Fingerprint(
    definingClass = "Lcom/vega/ad/impl/cutsame/FreeCutsameAdViewModel;",
    name = "D6",
    returnType = "V",
    parameters = listOf(
        "Landroid/os/Bundle;",
        "Lcom/vega/infrastructure/vm/ViewModelActivity;",
        "Lcom/vega/adapi/api/freeReward/RewardCallBack;",
        "Lcom/vega/adapi/data/reward/RewardAdTemplateInfo;",
    ),
)

private fun rewardCompletion(callbackRegister: String, returnObject: Boolean): String = buildString {
    appendLine("new-instance v0, Lcom/vega/adapi/api/freeReward/RewardShowResult;")
    appendLine("const/4 v1, 0x1")
    appendLine("const/4 v2, 0x0")
    appendLine("const-string v3, \"auto_skip\"")
    appendLine("invoke-direct {v0, v1, v2, v3}, Lcom/vega/adapi/api/freeReward/RewardShowResult;-><init>(ZZLjava/lang/String;)V")
    appendLine("invoke-interface {$callbackRegister, v0}, Lcom/vega/adapi/api/freeReward/RewardCallBack;->a(Lcom/vega/adapi/api/freeReward/RewardShowResult;)V")
    if (returnObject) {
        appendLine("sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;")
        append("return-object v0")
    } else {
        append("return-void")
    }
}

@Suppress("unused")
val capcutRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Disables CapCut ad configuration and splash gates, then completes export and template reward-ad callbacks through the app's supported auto-skip result.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute {
        (adGates + SplashAdService).forEach { it.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
        FreeEditExportRewardAd.method.addInstructions(0, rewardCompletion("p5", returnObject = true))
        T4RewardAd.method.addInstructions(0, rewardCompletion("p5", returnObject = false))
        FreeCutsameAd.method.addInstructions(0, rewardCompletion("p3", returnObject = false))
    }
}
