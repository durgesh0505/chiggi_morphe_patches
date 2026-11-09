package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags

private object HeliosValidDomainFingerprint : Fingerprint(
    definingClass = "Lcom/bytedance/helios/network/pipeline/ValidDomainManager;",
    name = "d",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;", "Ljava/util/Set;"),
)

@Suppress("unused")
val capcutAllowFunctionalHostsPatch = bytecodePatch(
    name = "Allow Functional Network Hosts",
    description = "Allows CapCut API and resource-CDN domain families through Helios' runtime domain policy so templates and downloadable assets are not rejected with HTTP 410. Other domains keep the original policy.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        HeliosValidDomainFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "capcutapi.com"
                invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
                move-result v0
                if-nez v0, :allow_domain
                const-string v0, "ibyteimg.com"
                invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
                move-result v0
                if-nez v0, :allow_domain
                const-string v0, "tiktokcdn-us.com"
                invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
                move-result v0
                if-eqz v0, :continue_policy
                :allow_domain
                const/4 v0, 0x1
                return v0
                :continue_policy
            """,
        )
    }
}
