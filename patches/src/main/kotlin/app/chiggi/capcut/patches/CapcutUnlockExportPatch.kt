package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants

private object TemplateExportUltraHdUtilsCFingerprint : Fingerprint(
    definingClass = "Lcom/vega/edit/base/utils/TemplateExportUltraHdUtils;",
    name = "c",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

private object TemplateExportUltraHdUtilsDFingerprint : Fingerprint(
    definingClass = "Lcom/vega/edit/base/utils/TemplateExportUltraHdUtils;",
    name = "d",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

private object TemplateExportUltraHdUtilsEFingerprint : Fingerprint(
    definingClass = "Lcom/vega/edit/base/utils/TemplateExportUltraHdUtils;",
    name = "e",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

private object TemplateExportUltraHdUtilsJFingerprint : Fingerprint(
    definingClass = "Lcom/vega/edit/base/utils/TemplateExportUltraHdUtils;",
    name = "j",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

private object TemplateExportUltraHdUtilsQFingerprint : Fingerprint(
    definingClass = "Lcom/vega/edit/base/utils/TemplateExportUltraHdUtils;",
    name = "q",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

@Suppress("unused")
val capcutUnlockExportPatch = bytecodePatch(
    name = "Unlock Ultra HD Export",
    description = "Forces CapCut Ultra HD / high-resolution export gates to unlocked so 4K export options stay available.",
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

        forceTrue(TemplateExportUltraHdUtilsCFingerprint)
        forceTrue(TemplateExportUltraHdUtilsDFingerprint)
        forceTrue(TemplateExportUltraHdUtilsEFingerprint)
        forceTrue(TemplateExportUltraHdUtilsJFingerprint)
        forceTrue(TemplateExportUltraHdUtilsQFingerprint)

        println("[Unlock Ultra HD Export] Applied $patched hooks -> ultra HD export gates forced to unlocked.")
    }
}
