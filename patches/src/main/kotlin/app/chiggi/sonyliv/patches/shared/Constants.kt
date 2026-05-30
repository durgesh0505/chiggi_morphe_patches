package app.chiggi.sonyliv.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_SONYLIV = Compatibility(
        name = "SonyLIV",
        packageName = "com.sonyliv",
        // Distributed as split APKs (APKM bundle) on APKMirror.
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x7460A4, // SonyLIV accent (apkm info.json accent_color = 7460a4), 0xRRGGBB
        targets = listOf(
            AppTarget(
                version = "6.23.1",
                minSdk = 22,
            ),
        ),
    )
}
