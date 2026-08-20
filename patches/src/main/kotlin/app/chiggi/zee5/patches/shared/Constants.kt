package app.chiggi.zee5.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // ZEE5 (com.graymatrix.did) Android TV / Chromecast-with-Google-TV build (leanback launcher,
    // touchscreen not required). Standard Android/Kotlin app (com.zee5.android.*), 8 dex, no
    // Flutter/Unity/RN. Real class/method names are largely kept in the com.zee5.android.ui.player.*
    // presentation layer, so player patches anchor on named methods. Premium/content is server
    // entitlement (UMS GraphQL) + Widevine DRM and mid-roll ads are DAI (server-stitched), so those
    // are NOT client-patchable; only client-rendered surfaces (e.g. the player watermark logo) are.
    // Supplied as a single universal APK (arm64-v8a), pinned to 5.83.2.
    val COMPATIBILITY_ZEE5 = Compatibility(
        name = "ZEE5 (Android TV)",
        packageName = "com.graymatrix.did",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x6E1EE5, // fallback tint (Zee5 purple); Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "5.83.2",
                minSdk = 23,
            ),
        ),
    )
}
