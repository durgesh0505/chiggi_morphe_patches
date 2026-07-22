package app.chiggi.cryanalyzer.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Cry Analyzer (jp.firstascent.cryanalyzer) by First Ascent. Standard Android app, real class
    // names kept (light obfuscation). Premium is a direct Google Play Billing subscription
    // (monthly / half-year) tracked by jp.firstascent.cryanalyzer.utility.billing.BillingClientWrapper;
    // BillingClientWrapper.isPurchased(productIdentifier, subscriptionPlan) returns whether a product
    // is owned (state PURCHASED / PURCHASED_AND_ACKNOWLEDGED). Ads are AdMob + IronSource. Single
    // universal APK, pinned to 1.3.28.
    val COMPATIBILITY_CRYANALYZER = Compatibility(
        name = "Cry Analyzer",
        packageName = "jp.firstascent.cryanalyzer",
        // Supplied as a single universal APK (not a split bundle).
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4FC3F7, // fallback tint (light blue); Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "1.3.28",
                minSdk = 23,
            ),
        ),
    )
}
