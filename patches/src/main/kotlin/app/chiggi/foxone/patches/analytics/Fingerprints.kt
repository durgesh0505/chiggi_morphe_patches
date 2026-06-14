package app.chiggi.foxone.patches.analytics

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

private const val ANALYTICS_CLIENT =
    "Lcom/fox/android/foxkit/common/analytics/client/AnalyticsClient;"

/**
 * The (obfuscated) FOX AppsFlyer wrapper method that initialises and starts AppsFlyer
 * (calls AppsFlyerLib.init + AppsFlyerLib.start). Anchored on the AppsFlyerLib.start call so it
 * survives class/method renaming. No-oping it stops AppsFlyer from starting/tracking.
 */
internal object AppsFlyerStartFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        // Match by class + method name only (parameter types may be obfuscated).
        methodCall(
            definingClass = "Lcom/appsflyer/AppsFlyerLib;",
            name = "start",
        ),
    ),
)

/**
 * com.fox.android.foxkit.common.analytics.client.AnalyticsClient#logEvent(AnalyticsEventRequest)
 * The main FoxKit first-party telemetry sink (events -> Room DB -> WorkManager POST).
 */
internal object LogEventRequestFingerprint : Fingerprint(
    name = "logEvent",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/fox/android/foxkit/common/analytics/models/AnalyticsEventRequest;"),
    definingClass = ANALYTICS_CLIENT,
)

/** #logEvent(String) */
internal object LogEventStringFingerprint : Fingerprint(
    name = "logEvent",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ANALYTICS_CLIENT,
)

/** #logPerformanceEvent(String) */
internal object LogPerformanceEventFingerprint : Fingerprint(
    name = "logPerformanceEvent",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = ANALYTICS_CLIENT,
)
