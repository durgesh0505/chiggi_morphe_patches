package app.chiggi.googlephone.patches.callscreen

import app.chiggi.googlephone.patches.shared.Constants.COMPATIBILITY_GOOGLE_PHONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enableCallScreenPatch = bytecodePatch(
    name = "Enable call screen (call assist)",
    description = "Forces DobbyEnabledFn.isEnabled() true so the Call Screen / call-assist entry is " +
        "offered on any device (normally gated to the revelio/dobby Phenotype flags, off on non-Pixel). " +
        "GOTCHA: Call Screen transcription runs on an on-device SODA speech model hosted by Speech " +
        "Services by Google / the Google app, which ships in Pixel firmware. Without that model + a " +
        "downloaded offline language pack the feature takes the SODA_UNAVAILABLE path and dead-ends. The " +
        "toggle appears but Call Screen will not answer/transcribe on a non-Pixel unless the on-device " +
        "recognizer is already provisioned. DISABLED BY DEFAULT: forcing it true makes the app run the " +
        "call-screen/SODA init path on a call, which dead-ends (or throws) on a non-Pixel and can crash " +
        "the in-call flow. Opt in only for UI inspection on a Pixel.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GOOGLE_PHONE)

    execute {
        DobbyEnabledFnFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
