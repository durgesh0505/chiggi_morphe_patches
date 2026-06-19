package app.chiggi.arrowpuzzle.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Easybrain "Arrow Puzzle" is a Unity (IL2CPP) game; gameplay is native, but ads and analytics
    // run on the Android/DEX side through the Easybrain SDK (Unity calls them via a JNI bridge).
    // The DEX is NOT packed, so those layers are patchable. Class/method names are R8-obfuscated,
    // BUT the Unity JNI facades (AdsPlugin / AnalyticsPlugin) keep their public method names (the
    // C# <-> Java contract), so the ad/analytics hooks anchor on stable named methods. Pinned to
    // 1.7.0; a different version may still need re-fingerprinting.
    val COMPATIBILITY_ARROWPUZZLE = Compatibility(
        name = "Arrow Puzzle",
        packageName = "com.easybrain.arrow.puzzle.game",
        // Supplied as a single universal APK (not a split bundle).
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2D6CDF, // brand blue fallback tile (Manager extracts the real launcher icon)
        targets = listOf(
            AppTarget(
                version = "1.7.0",
                minSdk = 26,
            ),
        ),
    )
}
