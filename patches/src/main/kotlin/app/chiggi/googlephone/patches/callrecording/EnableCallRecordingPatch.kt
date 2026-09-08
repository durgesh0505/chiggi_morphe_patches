package app.chiggi.googlephone.patches.callrecording

import app.chiggi.googlephone.patches.shared.Constants.COMPATIBILITY_GOOGLE_PHONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enableCallRecordingPatch = bytecodePatch(
    name = "Enable call recording",
    description = "Turns on Google Phone's built-in call recorder in regions where Google hides it. " +
        "The recorder is gated on a client-side country allowlist that CanRecord.canRecordCall() " +
        "resolves to one boolean the record button reads; forcing it true offers recording everywhere. " +
        "Recording is on-device and works through the default-dialer telecom audio path (no privileged/" +
        "priv-app permission needed), so it functions on non-Pixel devices — but Google Phone MUST be " +
        "set as your default phone app. Recording calls is regulated in many places; check what is " +
        "allowed where you live.",
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
