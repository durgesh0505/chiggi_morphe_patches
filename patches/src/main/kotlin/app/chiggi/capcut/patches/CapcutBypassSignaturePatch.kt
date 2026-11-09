package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

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
    description = "Spoofs the official APK signature (SHA-256) for tamper telemetry, drops the server-driven signature blacklist, silences the abnormal-source toast, and disables the Godzilla inject-detect gate so re-signed builds are not blocked.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        var patched = 0

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

        println("[Bypass Signature Check] Applied $patched hooks -> APK signature spoofed to official, tamper gates disabled, security dialog suppressed, PGL signature verification stubbed.")
    }
}
