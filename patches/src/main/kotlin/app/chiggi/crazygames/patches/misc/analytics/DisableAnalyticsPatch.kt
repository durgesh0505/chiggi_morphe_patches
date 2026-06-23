package app.chiggi.crazygames.patches.misc.analytics

import app.chiggi.crazygames.patches.shared.Constants.COMPATIBILITY_CRAZYGAMES
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

// Capacitor PluginCall.resolve() (empty success); z() is the R8-renamed resolve().
private val RESOLVE_EMPTY = """
    invoke-virtual {p1}, Lcom/getcapacitor/Y;->z()V
    return-void
"""

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Stops native Sentry crash/telemetry upload by neutering the Capacitor Sentry " +
        "plugin's native init (io.sentry.capacitor.SentryCapacitor.initNativeSdk), so it resolves " +
        "without starting the native Sentry SDK. Firebase auth, ConfigCat and push are unaffected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CRAZYGAMES)

    execute {
        SentryInitNativeSdkFingerprint.method.addInstructions(0, RESOLVE_EMPTY)
    }
}
