package app.chiggi.crazygames.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // CrazyGames is a Capacitor (Ionic) WebView shell around the CrazyGames Next.js portal
    // (app.crazygames.com). The native ad layer is the community Capacitor AdMob plugin
    // (com.getcapacitor.community.admob.AdMob) in plain DEX: the class name is NOT obfuscated and
    // its method names are the Capacitor reflection contract, so the ad hooks anchor on stable
    // named methods. Only the internal Capacitor helper classes (PluginCall/JSObject = Y/L and
    // resolve = z/A) are R8-obfuscated, so the injected bytecode is version-specific. Pinned to
    // 1.6.7; a different version may need re-fingerprinting.
    //
    // Note: in-page web video ads served remotely inside the WebView (from crazygames.com) are NOT
    // in the DEX and cannot be removed here — only the native interstitial/banner/rewarded SDK path
    // is patchable.
    val COMPATIBILITY_CRAZYGAMES = Compatibility(
        name = "CrazyGames",
        packageName = "com.crazygames.crazygamesapp",
        // Supplied as a single universal APK (not a split bundle).
        apkFileType = ApkFileType.APK,
        appIconColor = 0x0C0D14, // real ic_launcher_background (dark navy); Manager extracts the icon
        targets = listOf(
            AppTarget(
                version = "1.6.7",
                minSdk = 24,
            ),
        ),
    )
}
