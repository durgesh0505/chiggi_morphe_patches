package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

private object WatermarkBusinessFunctionQ0Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/WatermarkBusinessFunction;",
    name = "Q0",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/vega/subscriptionapi/biz/function/WatermarkSceneParams;"),
)

private object WatermarkBusinessFunctionC4Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/WatermarkBusinessFunction;",
    name = "C4",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;"),
)

private object WatermarkBusinessFunctionF4Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/WatermarkBusinessFunction;",
    name = "F4",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/vega/subscriptionapi/biz/function/WatermarkBusinessParam;", "Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;"),
)

private object WatermarkBusinessFunctionV4Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/WatermarkBusinessFunction;",
    name = "v4",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;"),
)

private object WatermarkBusinessFunctionY2Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/WatermarkBusinessFunction;",
    name = "y2",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object EditConfigKFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/editor/EditConfig;",
    name = "k",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

@Suppress("unused")
val capcutRemoveWatermarkPatch = bytecodePatch(
    name = "Remove Watermark",
    description = "Forces CapCut watermark-removal gates and default ending gate to allow export without watermark or outro.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        var patched = 0

        fun forceTrue(fp: Fingerprint) {
            fp.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
            patched++
        }

        fun forceFalse(fp: Fingerprint) {
            fp.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
            patched++
        }

        forceTrue(WatermarkBusinessFunctionQ0Fingerprint)
        forceTrue(WatermarkBusinessFunctionC4Fingerprint)
        forceTrue(WatermarkBusinessFunctionF4Fingerprint)
        forceTrue(WatermarkBusinessFunctionV4Fingerprint)
        forceFalse(WatermarkBusinessFunctionY2Fingerprint)
        forceFalse(EditConfigKFingerprint)

        println("[Remove Watermark] Applied $patched hooks -> watermark and default ending gates forced to allow removal.")
    }
}
