package app.chiggi.crazygames.patches.ads

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.getcapacitor.community.admob.AdMob — the Capacitor AdMob plugin. The class name is NOT
 * obfuscated and the @PluginMethod method names are kept (Capacitor invokes them by reflection from
 * the WebView bridge), so every native ad show/prepare crosses one of these named methods. Each
 * takes a single PluginCall (R8-renamed to Lcom/getcapacitor/Y;) and returns void.
 */
private const val ADMOB_CLASS = "Lcom/getcapacitor/community/admob/AdMob;"
private const val PLUGIN_CALL = "Lcom/getcapacitor/Y;"

private val PUBLIC = listOf(AccessFlags.PUBLIC)

// --- Interstitial ---
internal object ShowInterstitialFingerprint : Fingerprint(
    name = "showInterstitial",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

internal object PrepareInterstitialFingerprint : Fingerprint(
    name = "prepareInterstitial",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

// --- Banner ---
internal object ShowBannerFingerprint : Fingerprint(
    name = "showBanner",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

internal object ResumeBannerFingerprint : Fingerprint(
    name = "resumeBanner",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

// --- Rewarded (prepare -> resolve empty so the web thinks an ad is ready) ---
internal object PrepareRewardVideoAdFingerprint : Fingerprint(
    name = "prepareRewardVideoAd",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

internal object PrepareRewardInterstitialAdFingerprint : Fingerprint(
    name = "prepareRewardInterstitialAd",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

// --- Rewarded (show -> auto-grant the reward without showing an ad) ---
internal object ShowRewardVideoAdFingerprint : Fingerprint(
    name = "showRewardVideoAd",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)

internal object ShowRewardInterstitialAdFingerprint : Fingerprint(
    name = "showRewardInterstitialAd",
    accessFlags = PUBLIC,
    returnType = "V",
    parameters = listOf(PLUGIN_CALL),
    definingClass = ADMOB_CLASS,
)
