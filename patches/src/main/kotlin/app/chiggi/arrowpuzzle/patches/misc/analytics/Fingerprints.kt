package app.chiggi.arrowpuzzle.patches.misc.analytics

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.easybrain.analytics.unity.AnalyticsPlugin#AnalyticsSendEvent(String, String) — the named JNI
 * entry the Unity game uses to send every gameplay/monetization analytics event. No-opping it stops
 * the game's own event upload. NAMED class + JNI method name => version-robust.
 */
internal object AnalyticsSendEventFingerprint : Fingerprint(
    name = "AnalyticsSendEvent",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
    definingClass = "Lcom/easybrain/analytics/unity/AnalyticsPlugin;",
)

/**
 * The central Easybrain analytics collector (AnalyticsController.kt, obfuscated to xb.i): the single
 * method every analytics event — Unity AND native-Android (ad SDK) originated — funnels through
 * before being fanned out to the registered adapters (Firebase, etc.). No-opping it stops ALL event
 * transmission while leaving SDK init / adapter registration intact (do NOT touch the constructor —
 * the ad stack depends on the controller existing).
 *
 * Obfuscated (xb.i / event type com.easybrain.analytics.event.b) and pinned to 1.7.0; re-verify on
 * app updates.
 */
internal object AnalyticsControllerLogEventFingerprint : Fingerprint(
    name = "j",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/easybrain/analytics/event/b;"),
    definingClass = "Lxb/i;",
)
