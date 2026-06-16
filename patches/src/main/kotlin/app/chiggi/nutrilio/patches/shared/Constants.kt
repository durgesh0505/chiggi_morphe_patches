package app.chiggi.nutrilio.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // No version targets => compatible with any Nutrilio version (the Manager will offer the patches
    // regardless of installed version). Caveat: Nutrilio is R8-obfuscated, so the bytecode patches
    // (Unlock Plus, Add food search bar) match obfuscated names/ids that only verifiably resolve on
    // 1.20.2; they are hardened (string-anchored fingerprint, runtime id resolution) to maximise the
    // chance of matching other versions but are not guaranteed. The resource/manifest patches
    // (Disable analytics, Change app name, Change package name) are version-agnostic.
    val COMPATIBILITY_NUTRILIO = Compatibility(
        name = "Nutrilio",
        packageName = "net.nutrilio",
        // Distributed as split APKs (base + config.arm64_v8a + config.xxhdpi); supply as .apks.
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x36D4C3, // Nutrilio mint accent (colors.xml "mint" = #36d4c3), 0xRRGGBB
        // A single target with a null version means "any Nutrilio version" (Morphe rejects an empty
        // target list).
        targets = listOf(
            AppTarget(
                version = null,
                minSdk = 26,
            ),
        ),
    )
}
