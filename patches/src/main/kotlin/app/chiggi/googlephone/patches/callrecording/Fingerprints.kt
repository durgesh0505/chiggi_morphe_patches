package app.chiggi.googlephone.patches.callrecording

import app.morphe.patcher.Fingerprint

/**
 * The call-recording availability gate: com.android.dialer.callrecording.impl.canrecord.CanRecord
 * .canRecordCall() (obfuscated to defpackage.iwd.a() in v235.0). Returns whether call recording is
 * offered — checks a geofence/country flag, a Build.DEVICE OEM allowlist, the call_recording_audio
 * system feature, a "fermat" kill-switch, and finally the G__enable_call_recording flag (default
 * false). Forcing it true makes the recording UI appear.
 *
 * Anchored on the surviving zqz log-path literal + the unique final-branch decision string, plus
 * returnType Z / no params — the obfuscated class name is not stable across builds.
 */
internal object CanRecordCallFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    strings = listOf(
        "com/android/dialer/callrecording/impl/canrecord/CanRecord",
        "Call recording is disabled by the call recording flag",
    ),
)
