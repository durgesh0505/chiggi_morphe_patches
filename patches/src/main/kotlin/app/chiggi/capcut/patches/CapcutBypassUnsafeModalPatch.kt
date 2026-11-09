package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private object LynxUnsafeModalDefaultFingerprint : Fingerprint(
    definingClass = "Lcom/vega/lynx/LynxSchemaConfig;",
    name = "<init>",
    strings = listOf("image_lynx_global_unsafe_pkg_modal"),
)

private object RuntimeCrackGuideSchemaFingerprint : Fingerprint(
    definingClass = "Lcom/vega/paas/LynxSchemeConfigCapCutImpl;",
    name = "t0",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)

private object CentralLynxOpenFingerprint : Fingerprint(
    definingClass = "Lcom/vega/core/ext/FunctionKt;",
    name = "j",
    returnType = "Z",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Z",
        "Lorg/json/JSONObject;",
        "Landroid/content/Intent;",
        "Landroid/os/Bundle;",
    ),
)

private object DeeplinkActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/vega/launcher/precondition/DeeplinkActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

private const val UNSAFE_MODAL_URL =
    "capcut://main/lynx?channel=image_lynx_global_unsafe_pkg_modal&bundle=template.js&hide_loading=0&hide_nav_bar=1&immersive_mode=1&enter_mode=modal&present_style=over_full_screen&loading_bgcolor=00000000&dynamic=3&exit_mode=none"

private const val TEMPLATE_PORTAL_URL =
    "capcut://lynxview?channel=image_lynx_template_portal&bundle=template.js"

@Suppress("unused")
val capcutBypassUnsafeModalPatch = bytecodePatch(
    name = "Bypass Unsafe Package Modal",
    description = "Blocks the unsafe-package security modal at CapCut's central Lynx opener and replaces default/runtime crack-guide schemas with the template portal.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        val method = LynxUnsafeModalDefaultFingerprint.method
        val instructions = method.implementation?.instructions?.toList()
            ?: error("[Bypass Unsafe Package Modal] Target <init> has no instructions.")

        val matches = instructions.mapIndexedNotNull { index, ins ->
            val ref = (ins as? ReferenceInstruction)?.reference
            if (ref is StringReference && ref.string == UNSAFE_MODAL_URL) {
                val reg = (ins as? OneRegisterInstruction)?.registerA
                    ?: error("[Bypass Unsafe Package Modal] const-string has no register.")
                index to reg
            } else {
                null
            }
        }

        if (matches.isEmpty()) {
            error("[Bypass Unsafe Package Modal] Unsafe modal URL default not found.")
        }

        matches.sortedByDescending { it.first }.forEach { (index, reg) ->
            method.addInstructions(
                index + 1,
                "const-string v$reg, \"$TEMPLATE_PORTAL_URL\"",
            )
        }

        RuntimeCrackGuideSchemaFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$TEMPLATE_PORTAL_URL"
                return-object v0
            """,
        )

        CentralLynxOpenFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "image_lynx_global_unsafe_pkg_modal"
                invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                move-result v0
                if-eqz v0, :continue_open
                const/4 v0, 0x1
                return v0
                :continue_open
            """,
        )

        DeeplinkActivityOnCreateFingerprint.method.addInstructions(
            0,
            """
                move-object/from16 v2, p0
                invoke-virtual {v2}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
                move-result-object v0
                if-eqz v0, :continue_deeplink
                invoke-virtual {v0}, Landroid/content/Intent;->getDataString()Ljava/lang/String;
                move-result-object v0
                if-eqz v0, :continue_deeplink
                const-string v1, "image_lynx_global_unsafe_pkg_modal"
                invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                move-result v0
                if-eqz v0, :continue_deeplink
                move-object/from16 v3, p1
                invoke-static {v2, v3}, Lcom/bytedance/helios/statichook/lifecycle/ActivityLifecycleHook;->a(Landroid/app/Activity;Landroid/os/Bundle;)V
                invoke-super {v2, v3}, Lcom/vega/ui/start/BaseInfraActivity;->onCreate(Landroid/os/Bundle;)V
                invoke-virtual {v2}, Landroid/app/Activity;->finish()V
                return-void
                :continue_deeplink
            """,
        )

        println("[Bypass Unsafe Package Modal] Blocked unsafe modal URLs at central and deep-link routing, rewrote ${matches.size} default(s), and forced the runtime schema to the template portal.")
    }
}
