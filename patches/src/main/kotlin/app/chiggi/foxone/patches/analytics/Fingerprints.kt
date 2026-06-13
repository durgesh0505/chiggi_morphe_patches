package app.chiggi.foxone.patches.analytics

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val ANALYTICS_CLIENT =
    "Lcom/fox/android/foxkit/common/analytics/client/AnalyticsClient;"

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
