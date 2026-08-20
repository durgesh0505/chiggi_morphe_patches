package app.chiggi.hotstar.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // JioHotstar (in.startv.hotstar). Heavily R8-obfuscated, 10+ dex. Target is the ANDROID TV build,
    // distributed as a split bundle (.apkm): base.apk declares leanback REQUIRED + touchscreen
    // not-required and is leanback-launchable, i.e. a real D-pad TV UI that installs natively on
    // Android TV / Google TV. (The "gtv-streamer" arm64 and "chromecast-gtv" v7a universal .apk
    // downloads are phone builds — portrait, touchscreen-required — and are NOT the target.) Supply as
    // .apkm (base + v7a/arm64 + xhdpi + langs); morphe merges the splits into one installable APK.
    //
    // Ads are server-side stitched (SSAI) and premium is server-entitlement + Widevine DRM, so neither
    // is client-patchable; the patch set targets telemetry, the client-side VPN/integrity self-report,
    // branding and player-UX. Pinned to 26.06.22.3 (obfuscation shifts per release). NOTE: the app
    // sends a server-validated device-integrity attestation and requests Play Integrity tokens, so a
    // re-signed build may be rejected on the server regardless of these patches — verify login/
    // playback on device.
    val COMPATIBILITY_HOTSTAR = Compatibility(
        name = "JioHotstar (Android TV)",
        packageName = "in.startv.hotstar",
        // Android TV (leanback) build. Input is a dual-ABI UNIVERSAL apk merged from the .apkm (base +
        // arm64-v8a + armeabi-v7a splits) via APKEditor, so patching with Morphe on a phone can't drop
        // the TV's arch — the output installs on both 64-bit and 32-bit Android TV / Fire TV / Chromecast.
        // Build/supply workspace/input/hotstar/hotstar-universal.apk.
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1F80E0, // Hotstar brand blue fallback (Manager extracts the real icon)
        targets = listOf(
            AppTarget(
                version = "26.06.22.3",
                minSdk = 21,
            ),
        ),
    )
}
