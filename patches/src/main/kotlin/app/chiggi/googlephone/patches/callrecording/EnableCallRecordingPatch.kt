package app.chiggi.googlephone.patches.callrecording

import app.chiggi.googlephone.patches.shared.Constants.COMPATIBILITY_GOOGLE_PHONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enableCallRecordingPatch = bytecodePatch(
    name = "Enable call recording",
    description = "Forces CanRecord.canRecordCall() true so the call-recording UI is offered on any " +
        "device (it is normally gated to a country/OEM allowlist + the G__enable_call_recording flag). " +
        "GOTCHA: the recorder captures via AudioRecord(VOICE_CALL) + AudioPolicy, which need the " +
        "signature|privileged CAPTURE_AUDIO_OUTPUT permission (only granted to /system/priv-app). On a " +
        "re-signed sideload this is a normal app, so recordings are empty/silent and may error — the " +
        "toggle appears but does not functionally record. Use your Samsung dialer's native recording for " +
        "real recordings.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GOOGLE_PHONE)

    execute {
        CanRecordCallFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
