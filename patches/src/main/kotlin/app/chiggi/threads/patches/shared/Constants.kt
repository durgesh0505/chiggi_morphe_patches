package app.chiggi.threads.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Threads (Meta codename "Barcelona") is built from the Instagram codebase and is heavily
    // R8-obfuscated. Obfuscated class/method names shift on nearly every Meta release, so a
    // null ("any") version is NOT realistic here (and the Morphe Manager cannot parse a null
    // version anyway — it aborts the whole bundle). The bytecode hooks are anchored on NAMED
    // classes (com.instagram.feed.media.Media, BarcelonaSpoolFeedCacheHandler) so they tolerate
    // minor drift, but a different version may still need re-fingerprinting.
    val COMPATIBILITY_THREADS = Compatibility(
        name = "Threads",
        packageName = "com.instagram.barcelona",
        // Distributed as split APKs (base + config.arm64_v8a + config.<dpi>); supply as .apks.
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x000000, // Threads brand black (info.json accent_color 000000), 0xRRGGBB
        targets = listOf(
            AppTarget(
                version = "434.0.0.41.74",
                minSdk = 28,
            ),
        ),
    )
}
