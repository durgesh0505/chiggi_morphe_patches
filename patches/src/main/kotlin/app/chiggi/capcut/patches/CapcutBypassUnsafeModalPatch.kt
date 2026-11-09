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

private const val UNSAFE_MODAL_URL =
    "capcut://main/lynx?channel=image_lynx_global_unsafe_pkg_modal&bundle=template.js&hide_loading=0&hide_nav_bar=1&immersive_mode=1&enter_mode=modal&present_style=over_full_screen&loading_bgcolor=00000000&dynamic=3&exit_mode=none"

private const val TEMPLATE_PORTAL_URL =
    "capcut://lynxview?channel=image_lynx_template_portal&bundle=template.js"

@Suppress("unused")
val capcutBypassUnsafeModalPatch = bytecodePatch(
    name = "Bypass Unsafe Package Modal",
    description = "Replaces the unsafe-package modal fallback URL in the Lynx schema defaults with the real template portal URL so region-blocked or untrusted-client fallbacks open templates instead of the blocking security notice.",
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

        println("[Bypass Unsafe Package Modal] Applied ${matches.size} hook(s) -> unsafe modal fallback points at template portal.")
    }
}
