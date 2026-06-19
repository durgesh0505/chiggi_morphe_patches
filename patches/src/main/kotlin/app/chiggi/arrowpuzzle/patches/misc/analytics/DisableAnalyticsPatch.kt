package app.chiggi.arrowpuzzle.patches.misc.analytics

import app.chiggi.arrowpuzzle.patches.shared.Constants.COMPATIBILITY_ARROWPUZZLE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Stops Easybrain analytics event upload without breaking SDK init (the ad stack depends on the
 * analytics controller existing). Two cuts:
 *  1. AnalyticsPlugin.AnalyticsSendEvent -> no-op : kills Unity-originated game events (named hook).
 *  2. AnalyticsController(xb.i).j -> no-op       : central collector all events funnel through.
 *
 * "Safe depth": only the transmit methods are neutered; constructors / adapter registration stay
 * intact. Pair with "Remove AD_ID permission" to also block advertising-id collection.
 */
@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Stops Easybrain analytics event upload (Unity AnalyticsPlugin send + the central " +
        "AnalyticsController collector). SDK init is left intact so ad removal stays stable. Does " +
        "not disable deep GMS/Firebase auto-collection; pair with Remove AD_ID for advertising-id.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ARROWPUZZLE)

    execute {
        listOf(
            AnalyticsSendEventFingerprint,
            AnalyticsControllerLogEventFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }
    }
}
