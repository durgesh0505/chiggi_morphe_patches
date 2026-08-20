package app.chiggi.sonyliv.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_SONYLIV = Compatibility(
        name = "SonyLIV (Android TV)",
        packageName = "com.sonyliv",
        // Android TV (leanback) build. Input is a dual-ABI UNIVERSAL apk merged from the APKMirror
        // .apkm (base + arm64-v8a + armeabi-v7a splits) via APKEditor, so Morphe on a phone can't drop
        // the TV's arch — the output installs on both 64-bit and 32-bit Android TV / Fire TV. Build/
        // supply workspace/input/sonyliv/sonyliv-universal.apk.
        apkFileType = ApkFileType.APK,
        appIconColor = 0x7460A4, // SonyLIV accent (apkm info.json accent_color = 7460a4), 0xRRGGBB
        targets = listOf(
            AppTarget(
                version = "6.23.1",
                minSdk = 22,
            ),
        ),
    )
}
