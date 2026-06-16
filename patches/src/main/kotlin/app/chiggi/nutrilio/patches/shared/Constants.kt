package app.chiggi.nutrilio.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Target the verified version explicitly. A null ("any") version is the patcher's sanctioned
    // way to support all versions, but Morphe Manager cannot parse a null version in the source
    // index — it aborts the whole bundle (0 patches / version N/A / error icon). Nutrilio is also
    // R8-obfuscated, so the bytecode patches (Unlock Plus, Add food search bar) only verifiably
    // resolve on 1.20.2 anyway; they are hardened (string-anchored fingerprint, runtime id
    // resolution) to tolerate minor drift, but a different version may need re-fingerprinting.
    val COMPATIBILITY_NUTRILIO = Compatibility(
        name = "Nutrilio",
        packageName = "net.nutrilio",
        // Distributed as split APKs (base + config.arm64_v8a + config.xxhdpi); supply as .apks.
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x36D4C3, // Nutrilio mint accent (colors.xml "mint" = #36d4c3), 0xRRGGBB
        targets = listOf(
            AppTarget(
                version = "1.20.2",
                minSdk = 26,
            ),
        ),
    )
}
