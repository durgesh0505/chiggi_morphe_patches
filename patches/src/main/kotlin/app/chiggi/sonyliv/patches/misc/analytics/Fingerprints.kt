package app.chiggi.sonyliv.patches.misc.analytics

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.sonyliv.Analytics.AppsFlyerManager#isAppsFlyerSupported()
 *
 * Currently `return true`. Every AppsFlyer init and event-tracking call in this class is gated
 * behind isAppsFlyerSupported(), so forcing it to false disables AppsFlyer entirely.
 * Class/method names are not obfuscated in 6.23.1.
 */
internal object IsAppsFlyerSupportedFingerprint : Fingerprint(
    name = "isAppsFlyerSupported",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    definingClass = "Lcom/sonyliv/Analytics/AppsFlyerManager;",
)
