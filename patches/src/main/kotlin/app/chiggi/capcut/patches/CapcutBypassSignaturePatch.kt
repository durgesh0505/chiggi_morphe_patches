package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private object AppUtilsGetSignatureFingerprint : Fingerprint(
    definingClass = "Lcom/lm/components/utils/AppUtils;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)

private object JniToastShowApkErrorFingerprint : Fingerprint(
    definingClass = "Lcom/xt/retouch/baseui/JniToast;",
    name = "showApkError",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

private object InjectSignatureBlFingerprint : Fingerprint(
    definingClass = "Lcom/xt/retouch/painter/PainterSdkImpl\$injectSignatureBl\$1;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
)

private object PainterInitReportFingerprint : Fingerprint(
    definingClass = "Lcom/xt/retouch/painter/PainterSdkImpl\$init\$2;",
    name = "invokeSuspend",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
)

private object GodzillaGateFingerprint : Fingerprint(
    definingClass = "Lcom/vega/launcher/godzilla/GodzillaHookedTagManager;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf(),
)

private object CrackingDialogRenderFingerprint : Fingerprint(
    definingClass = "Lcom/vega/launcher/network/interceptors/CrackingInterceptor\$showGlobalSecurityDialog\$1;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf(),
    strings = listOf("lynx_overlay_id"),
)

private object BusinessSignDialogFingerprint : Fingerprint(
    definingClass = "Lcom/vega/launcher/init/InitManagerEx\$tryShowBusinessSignDialog\$1;",
    name = "invokeSuspend",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf("creator_sign_alert"),
)

private object NetworkInitHookFingerprint : Fingerprint(
    definingClass = "Lcom/vega/launcher/init/core/hook/NetworkInitHook;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

private object NetworkInitTaskFingerprint : Fingerprint(
    definingClass = "Lcom/vega/launcher/start/schedule/tasks/NetworkInitTask;",
    name = "run",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

private object TemplateFeedCrackingInterceptFingerprint : Fingerprint(
    definingClass = "Lcom/xt/retouch/applauncher/core/interceptor/CrackingInterceptor;",
    name = "intercept",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Lcom/bytedance/retrofit2/SsResponse;",
    parameters = listOf("Lcom/bytedance/retrofit2/intercept/Interceptor${'$'}Chain;"),
)

private object PglSignatureCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pgl/ssdk/t;",
    name = "g",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf(),
)

private object PglSignatureValidateFingerprint : Fingerprint(
    definingClass = "Lcom/pgl/ssdk/t;",
    name = "d",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf(),
)

@Suppress("unused")
val capcutBypassSignaturePatch = bytecodePatch(
    name = "Bypass Signature Check",
    description = "Spoofs the official APK signature, suppresses local and server-driven cracking notices, prevents cracking-verdict persistence, and disables Godzilla and PGL integrity gates so re-signed builds are not blocked.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    dependsOn(capcutCrackingPersistencePatch)

    execute {
        var patched = 0

        fun removeNativeCrackingInterceptor(fp: Fingerprint) {
            val method = fp.method
            val instructions = method.implementation?.instructions?.toList()
                ?: error("[Bypass Signature Check] Network init target has no instructions.")
            val arrayListOfIndex = instructions.indexOfFirst { instruction ->
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                reference?.name == "arrayListOf" &&
                    reference.definingClass == "Lkotlin/collections/CollectionsKt__CollectionsKt;"
            }
            if (arrayListOfIndex < 0) {
                error("[Bypass Signature Check] Network interceptor arrayListOf call not found.")
            }
            val listRegister = (instructions.getOrNull(arrayListOfIndex + 1) as? OneRegisterInstruction)?.registerA
                ?: error("[Bypass Signature Check] Network interceptor list result register not found.")
            if (listRegister == 0) {
                error("[Bypass Signature Check] Network interceptor list unexpectedly uses scratch register v0.")
            }
            method.addInstructions(
                arrayListOfIndex + 2,
                """
                    const/4 v0, 0x6
                    invoke-virtual {v$listRegister, v0}, Ljava/util/ArrayList;->remove(I)Ljava/lang/Object;
                """,
            )
            patched++
        }

        AppUtilsGetSignatureFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "6F:33:FD:44:6F:6E:83:A7:67:D4:FF:F9:C1:9C:EC:F7:60:59:81:4E:A6:A7:EA:71:DF:1D:0D:EF:2D:75:DB:31"
                return-object v0
            """,
        )
        patched++

        JniToastShowApkErrorFingerprint.method.addInstructions(
            0,
            "return-void",
        )
        patched++

        InjectSignatureBlFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v0
            """,
        )
        patched++

        PainterInitReportFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v0
            """,
        )
        patched++

        GodzillaGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
        patched++


        CrackingDialogRenderFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v0
            """,
        )
        patched++

        BusinessSignDialogFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v0
            """,
        )
        patched++

        removeNativeCrackingInterceptor(NetworkInitHookFingerprint)
        removeNativeCrackingInterceptor(NetworkInitTaskFingerprint)

        TemplateFeedCrackingInterceptFingerprint.method.addInstructions(
            0,
            """
                invoke-interface {p1}, Lcom/bytedance/retrofit2/intercept/Interceptor${'$'}Chain;->request()Lcom/bytedance/retrofit2/client/Request;
                move-result-object v0
                invoke-interface {p1, v0}, Lcom/bytedance/retrofit2/intercept/Interceptor${'$'}Chain;->proceed(Lcom/bytedance/retrofit2/client/Request;)Lcom/bytedance/retrofit2/SsResponse;
                move-result-object v0
                return-object v0
            """,
        )
        patched++

        PglSignatureCheckFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
        patched++

        PglSignatureValidateFingerprint.method.addInstructions(
            0,
            "return-void",
        )
        patched++

        println("[Bypass Signature Check] Applied $patched hooks -> signature spoofed, JNI cracking interceptors removed from both network stacks, security dialogs suppressed, and PGL verification stubbed.")
    }
}
