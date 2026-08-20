package app.chiggi.jiotv.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // JioTV+ (com.jio.media.jiotvplus, "Jiotv+: LiveTV & OTT") — native Android TV build (leanback,
    // PatchWall UI, real com.jio.* class names, lightly obfuscated), 5 dex. No Flutter/Unity/RN, no
    // PairIP/Play Integrity anti-tamper, so re-signing is clean. Ads run through the JioAds SDK
    // (JioAdView) for display ads and VMAPAdsHelper (wrapping Google IMA + SSAI) for player pre/mid/
    // post-roll + stitched ads — all client-side, removable at those chokepoints. Content is
    // server-entitlement + Widevine DRM gated (no client paywall flag), so there is no meaningful
    // premium unlock. Distributed as a split bundle (base + v7a + tvdpi + en); supply as .apks/.apkm.
    // Pinned to 2.6.4_2076.
    val COMPATIBILITY_JIOTV = Compatibility(
        name = "Jiotv+: LiveTV & OTT (Android TV)",
        packageName = "com.jio.media.jiotvplus",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0C2074, // Jio brand blue fallback (Manager extracts the real launcher icon)
        targets = listOf(
            AppTarget(
                version = "2.6.4_2076",
                minSdk = 21,
            ),
        ),
    )
}
