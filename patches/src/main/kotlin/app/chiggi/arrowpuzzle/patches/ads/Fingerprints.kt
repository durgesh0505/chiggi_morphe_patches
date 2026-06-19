package app.chiggi.arrowpuzzle.patches.ads

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.easybrain.ads.unity.AdsPlugin — the static JNI facade between Unity (C#/il2cpp) and the
 * Easybrain Android ad SDK. EVERY ad show/availability decision crosses this boundary, and the
 * method names are the C# <-> Java JNI contract, so they are NOT obfuscated (only the class's
 * internals are). Forcing the availability/show booleans to false and the void shows to no-op makes
 * the whole game believe no ad of any type is ever ready, at one named chokepoint — no need to
 * touch any mediation SDK (ironSource / AppLovin / AdMob / Pangle) or its init.
 */
private const val ADS_PLUGIN_CLASS = "Lcom/easybrain/ads/unity/AdsPlugin;"

private val ADS_PLUGIN = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)

// --- Interstitial ---
internal object IsInterstitialCachedFingerprint : Fingerprint(
    name = "IsInterstitialCached",
    accessFlags = ADS_PLUGIN,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object IsInterstitialReadyFingerprint : Fingerprint(
    name = "IsInterstitialReady",
    accessFlags = ADS_PLUGIN,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object ShowInterstitialFingerprint : Fingerprint(
    name = "ShowInterstitial",
    accessFlags = ADS_PLUGIN,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ADS_PLUGIN_CLASS,
)

// --- Rewarded ---
internal object IsRewardedCachedFingerprint : Fingerprint(
    name = "IsRewardedCached",
    accessFlags = ADS_PLUGIN,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object ShowRewardedFingerprint : Fingerprint(
    name = "ShowRewarded",
    accessFlags = ADS_PLUGIN,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ADS_PLUGIN_CLASS,
)

// --- Banner ---
internal object ShowBannerFingerprint : Fingerprint(
    name = "ShowBanner",
    accessFlags = ADS_PLUGIN,
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object ShowBannerWithPositionFingerprint : Fingerprint(
    name = "ShowBanner",
    accessFlags = ADS_PLUGIN,
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "I"),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object EnableBannerFingerprint : Fingerprint(
    name = "EnableBanner",
    accessFlags = ADS_PLUGIN,
    returnType = "V",
    parameters = listOf(),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object GetBannerHeightFingerprint : Fingerprint(
    name = "GetBannerHeight",
    accessFlags = ADS_PLUGIN,
    returnType = "I",
    parameters = listOf(),
    definingClass = ADS_PLUGIN_CLASS,
)

internal object GetBannerHeightWithArgFingerprint : Fingerprint(
    name = "GetBannerHeight",
    accessFlags = ADS_PLUGIN,
    returnType = "I",
    parameters = listOf("I"),
    definingClass = ADS_PLUGIN_CLASS,
)
