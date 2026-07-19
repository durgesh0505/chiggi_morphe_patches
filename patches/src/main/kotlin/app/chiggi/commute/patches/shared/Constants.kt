package app.chiggi.commute.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Does Not Commute (com.mediocre.commute) — a Mediocre native game: the engine and game logic
    // live in libcommute.so + Lua/XML assets (assets/*.lua.mp3, *.xml.mp3), so most of the game is
    // NOT in the DEX and is not bytecode-patchable. The DEX is only the Android shell. Premium is a
    // single Play IAP (com.mediocre.commute.premium) brokered by the Java class
    // com.mediocre.commute.AndroidStore, which the native side queries over the MainActivity.command
    // JNI bridge ("storeisrestored,<sku>" -> AndroidStore.isProductIdRestored() -> "true"/"false";
    // "storegetstatus" -> AndroidStore.getStatusAsString()). Because ownership is decided from those
    // Java getters, it is spoofable in DEX. There is no client ad SDK in the DEX (only Firebase /
    // GAID plumbing), so there is nothing ad-related to patch here. Single universal APK, pinned to
    // 1.5.5; a different version may need re-fingerprinting.
    val COMPATIBILITY_COMMUTE = Compatibility(
        name = "Does Not Commute",
        packageName = "com.mediocre.commute",
        // Supplied as a single universal APK (not a split bundle).
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2E7D6F, // fallback tint (icon is a webp); Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "1.5.5",
                minSdk = 26,
            ),
        ),
    )
}
