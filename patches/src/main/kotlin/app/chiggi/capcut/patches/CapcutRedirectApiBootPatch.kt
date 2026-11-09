package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private object EditorApiCapcutApiFingerprint : Fingerprint(
    strings = listOf("editor-api.capcutapi.com"),
)

private object FeedApiCapcutApiFingerprint : Fingerprint(
    strings = listOf("feed-api.capcutapi.com"),
)

private object CommerceApiCapcutApiFingerprint : Fingerprint(
    strings = listOf("commerce-api.capcutapi.com"),
)

private object PassportApiCapcutApiFingerprint : Fingerprint(
    strings = listOf("passport-api.capcutapi.com"),
)

private data class HostRedirect(
    val oldHost: String,
    val newHost: String,
    val fingerprint: Fingerprint,
)

private data class InstructionReplacement(
    val index: Int,
    val reg: Int,
    val oldHost: String,
    val newStr: String,
)

// Active functional host redirects.
// Note on pruned hosts (editor-api.capcut.com, i18n-api.faceueditor.com):
// APK string pool analysis of CapCut 19.9.0 shows both 'editor-api.capcut.com' (2 occurrences)
// and 'i18n-api.faceueditor.com' (2 occurrences) restricted to classes20.dex as non-executable
// pool entries (e.g. static field initializers, annotations, or unused descriptor remnants)
// rather than rewritable const-string instructions in method bodies.
// Per Rule R2 (Zero-Fingerprint-Mismatch / zero dead hooks), both are pruned so that
// fingerprints only target active, verified bytecode const-string endpoints.
// All functional traffic is carried by the 4 active capcutapi.com hosts (63 verified rewrites):
// - editor-api.capcutapi.com (19)
// - feed-api.capcutapi.com (34)
// - commerce-api.capcutapi.com (7)
// - passport-api.capcutapi.com (3)
private val FUNCTIONAL_HOST_REDIRECTS = listOf(
    HostRedirect(
        oldHost = "editor-api.capcutapi.com",
        newHost = "editor-api-v2-boot.capcutapi.com",
        fingerprint = EditorApiCapcutApiFingerprint,
    ),
    HostRedirect(
        oldHost = "feed-api.capcutapi.com",
        newHost = "feed-api-v2-boot.capcutapi.com",
        fingerprint = FeedApiCapcutApiFingerprint,
    ),
    HostRedirect(
        oldHost = "commerce-api.capcutapi.com",
        newHost = "commerce-api-v2-boot.capcutapi.com",
        fingerprint = CommerceApiCapcutApiFingerprint,
    ),
    HostRedirect(
        oldHost = "passport-api.capcutapi.com",
        newHost = "passport-api-v2-boot.capcutapi.com",
        fingerprint = PassportApiCapcutApiFingerprint,
    ),
)

// Telemetry, tracker, and anti-bot endpoints are explicitly excluded from redirection:
// - mon.capcutapi.com, mon-va.byteoversea.com, mon.isnssdk.com, mon.byteoversea.com (APM / Slardar)
// - tt-gecko-sg.capcutapi.com, gecko-sg.byteoversea.com (Gecko resource telemetry)
// - i.isnssdk.com, i.byteoversea.com, log.byteoversea.com (AppLog and network event logging)
// - vcs-sg.tiktokv.com, vcs-sg.byteintl.com, vcs-sg.byteoversea.com, vcs-va.byteoversea.com (BDTuring)
// - pitaya.tiktokv.com (Pitaya SDK telemetry)
// - ug-sg.byteoversea.com (User Growth telemetry)
// - mssdk-sg.byteoversea.com (Device registration / security SDK telemetry)
//
// These telemetry dispatchers are already neutralized in CapcutBlockTelemetryPatch
// and must remain inactive. Redirecting them would generate unnecessary network traffic
// and risk reopening tracking or anti-bot channels.
private val FORBIDDEN_TELEMETRY_HOSTS = listOf(
    "mon.capcutapi.com",
    "tt-gecko-sg.capcutapi.com",
    "i.isnssdk.com",
    "vcs-sg.tiktokv.com",
    "log.byteoversea.com",
    "i.byteoversea.com",
    "gecko-sg.byteoversea.com",
    "pitaya.tiktokv.com",
    "ug-sg.byteoversea.com",
    "mssdk-sg.byteoversea.com",
    "vcs-sg.byteoversea.com",
    "vcs-va.byteoversea.com",
    "vcs-sg.byteintl.com",
    "mon-va.byteoversea.com",
    "mon.isnssdk.com",
    "mon.byteoversea.com",
)

private fun escapeSmaliString(value: String): String = buildString {
    for (char in value) {
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\r' -> append("\\r")
            '\n' -> append("\\n")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
}

@Suppress("unused")
val capcutRedirectApiBootPatch = bytecodePatch(
    name = "Redirect API to v2 Boot",
    description = "For regions where CapCut's default API hosts are blocked, redirects editor, template/feed, commerce, and passport requests to official v2-boot endpoints. Leave off when the default hosts work or local DNS filtering blocks v2-boot hosts.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        // Collect candidate methods across target functional hosts using indexed string fingerprints.
        val candidateMethods = mutableMapOf<String, MutableMethod>()
        for (target in FUNCTIONAL_HOST_REDIRECTS) {
            val matches = target.fingerprint.matchAllOrNull()
            if (matches.isNullOrEmpty()) {
                error("[Redirect API to v2 Boot] Host '${target.oldHost}' not found in DEX.")
            }
            for (match in matches) {
                val method = match.method
                val sig = "${method.definingClass}->${method.name}(${method.parameterTypes.joinToString(",")}):${method.returnType}"
                candidateMethods.putIfAbsent(sig, method)
            }
        }

        val appliedCounts = mutableMapOf<String, Int>()

        for (method in candidateMethods.values) {
            val instructions = method.implementation?.instructions?.toList() ?: continue
            val replacements = mutableListOf<InstructionReplacement>()

            instructions.forEachIndexed { index, ins ->
                val ref = (ins as? ReferenceInstruction)?.reference
                if (ref !is StringReference) return@forEachIndexed
                val originalString = ref.string

                if (FORBIDDEN_TELEMETRY_HOSTS.any { originalString.contains(it) }) {
                    return@forEachIndexed
                }

                for (target in FUNCTIONAL_HOST_REDIRECTS) {
                    if (originalString.contains(target.oldHost)) {
                        val reg = (ins as? OneRegisterInstruction)?.registerA
                            ?: error("[Redirect API to v2 Boot] Instruction at index $index in ${method.definingClass}->${method.name} has no register.")
                        val newString = originalString.replace(target.oldHost, target.newHost)
                        replacements.add(
                            InstructionReplacement(
                                index = index,
                                reg = reg,
                                oldHost = target.oldHost,
                                newStr = newString,
                            )
                        )
                        break
                    }
                }
            }

            // Descending order ensures injected instructions do not invalidate earlier offsets.
            replacements.sortedByDescending { it.index }.forEach { replacement ->
                val escaped = escapeSmaliString(replacement.newStr)
                method.addInstructions(
                    replacement.index + 1,
                    "const-string v${replacement.reg}, \"$escaped\"",
                )
                appliedCounts[replacement.oldHost] = (appliedCounts[replacement.oldHost] ?: 0) + 1
            }
        }

        for (target in FUNCTIONAL_HOST_REDIRECTS) {
            val count = appliedCounts[target.oldHost] ?: 0
            if (count == 0) {
                error("[Redirect API to v2 Boot] Host '${target.oldHost}' resolved candidate methods but 0 const-string instructions were rewritten.")
            }
        }

        val totalRewrites = appliedCounts.values.sum()
        val breakdown = FUNCTIONAL_HOST_REDIRECTS.joinToString(", ") { "${it.oldHost}: ${appliedCounts[it.oldHost] ?: 0}" }
        println("[Redirect API to v2 Boot] Applied $totalRewrites rewrites -> $breakdown.")
    }
}
