package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

private object EffectConfigurationBuilderPlatformFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/effectmanager/EffectConfiguration\$Builder;",
    name = "platform",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Lcom/ss/android/ugc/effectmanager/EffectConfiguration\$Builder;",
    parameters = listOf("Ljava/lang/String;"),
)

private object EffectConfigurationBuilderDeviceIdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/effectmanager/EffectConfiguration\$Builder;",
    name = "deviceId",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Lcom/ss/android/ugc/effectmanager/EffectConfiguration\$Builder;",
    parameters = listOf("Ljava/lang/String;"),
)

private object EffectConfigurationSetDeviceIdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/effectmanager/EffectConfiguration;",
    name = "setDeviceId",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)

@Suppress("unused")
val capcutBypassEffectsPatch = bytecodePatch(
    name = "Bypass Effects Region Restriction",
    description = "Fixes effects, transitions, and templates failing to load (ByteDance Shark WAF block) by spoofing device_platform to 'windows' and resetting deviceId in effect requests.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        var patched = 0

        // Prepend const-string p1, "windows" so both the local platform field
        // and mKNEffectConfigBuilder receive "windows" instead of "android".
        EffectConfigurationBuilderPlatformFingerprint.method.addInstructions(
            0,
            """
                const-string p1, "windows"
            """,
        )
        patched++

        // Force device_id to "0" in effect requests so ByteDance Shark WAF
        // does not cross-reference the device ID with an Android registration.
        EffectConfigurationBuilderDeviceIdFingerprint.method.addInstructions(
            0,
            """
                const-string p1, "0"
            """,
        )
        patched++

        EffectConfigurationSetDeviceIdFingerprint.method.addInstructions(
            0,
            """
                const-string p1, "0"
            """,
        )
        patched++

        println("[Bypass Effects Region Restriction] Applied $patched hooks -> Shark WAF block bypassed.")
    }
}
