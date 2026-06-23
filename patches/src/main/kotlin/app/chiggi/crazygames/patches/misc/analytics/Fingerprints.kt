package app.chiggi.crazygames.patches.misc.analytics

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * io.sentry.capacitor.SentryCapacitor.initNativeSdk(PluginCall) — the Capacitor Sentry plugin's
 * native init entry. The class and method name are kept (Capacitor reflection contract); the
 * PluginCall param is R8-renamed to Lcom/getcapacitor/Y;. Neutering this so it resolves without
 * starting the native Sentry SDK stops crash/telemetry upload.
 */
internal object SentryInitNativeSdkFingerprint : Fingerprint(
    name = "initNativeSdk",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/getcapacitor/Y;"),
    definingClass = "Lio/sentry/capacitor/SentryCapacitor;",
)
