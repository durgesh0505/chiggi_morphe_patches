package app.chiggi.foxone.patches.analytics

import app.chiggi.foxone.patches.shared.Constants.COMPATIBILITY_FOXONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableFoxAnalyticsPatch = bytecodePatch(
    name = "Disable FoxKit analytics",
    description = "Disables the FoxKit first-party analytics pipeline by no-oping " +
        "AnalyticsClient.logEvent / logPerformanceEvent, so no analytics events are recorded " +
        "or uploaded.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FOXONE)

    execute {
        listOf(
            LogEventRequestFingerprint,
            LogEventStringFingerprint,
            LogPerformanceEventFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }
    }
}
