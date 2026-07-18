package app.chiggi.vimtv.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Vi Movies and TV (com.vimtv) — a YuppTV-based OTT (com.yupptv.ott.*), native Android TV build
    // (leanback, no touchscreen requirement). Lightly obfuscated (real class names kept), 2 dex, no
    // Flutter/Unity/RN, no anti-tamper/Play Integrity and no VPN/geo detection, so re-signing is
    // clean and no bypass is needed. Ads are client-driven (Google IMA CSAI + SSAI) via ad-tag URLs,
    // so they are removable at the source. Premium/content is server-entitlement + Widevine DRM, so
    // any client "subscribed" flag is UI-only. Distributed as a split bundle (base + v7a + tvdpi +
    // en); supply as .apks/.apkm. Pinned to 10.8.
    val COMPATIBILITY_VIMTV = Compatibility(
        name = "Vi Movies and TV",
        packageName = "com.vimtv",
        // Split bundle (base + config.armeabi_v7a + config.tvdpi + config.en).
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xEE1C25, // Vi brand red fallback (Manager extracts the real launcher icon)
        targets = listOf(
            AppTarget(
                version = "10.8",
                minSdk = 21,
            ),
        ),
    )
}
