package app.chiggi.bighunter.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Big Hunter (com.kakarod.bighunter) — Cocos2d-x native game (libMyGame.so). The Java layer under
    // kakarodJavaLibs.data.* is UNOBFUSCATED and is the JNI bridge for ads + IAP, so patches fingerprint
    // on definingClass + name (no opcode patterns). Ads = AppLovin MAX + AdMob + Unity mediation; IAP =
    // Google Play Billing (KKJPaymentGoogle). Both grant through native calls the Java side controls
    // (KKJAds*.rewardCallback(), KKJPaymentGoogle.applyProduct(String)); there is NO client receipt
    // check (verifyDeveloperPayload() → true) and no server validation, so free IAP is genuine. arm64,
    // pinned to 3.1.1 (versionCode 72).
    val COMPATIBILITY_BIG_HUNTER = Compatibility(
        name = "Big Hunter",
        packageName = "com.kakarod.bighunter",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4CAF50, // fallback tint; Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "3.1.1",
                minSdk = 23,
            ),
        ),
    )
}
