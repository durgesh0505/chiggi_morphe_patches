package app.chiggi.vimtv.patches.misc.update

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableForcedUpdatePatch = bytecodePatch(
    name = "Disable forced update",
    description = "Removes the 'update available' popup (both the forced/non-cancelable and the " +
        "optional variant) shown on splash/home. The pinned 10.8 re-signed build would otherwise be " +
        "nagged — or hard-blocked — to update whenever the server reports a newer version. The popup " +
        "is gated on ClientInfo.getVersionNumber(); forcing it to \"0\" makes the version check in " +
        "SplashActivity and MainActivity conclude no update is needed.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    execute {
        // "0" -> parseInt(0) <= 34 in SplashActivity (no popup) and 0 > 34 is false in MainActivity
        // (skips the forced block before getUpdateType() is called, so no NPE).
        GetVersionNumberFingerprint.method.addInstructions(0, """
            const-string v0, "0"
            return-object v0
        """)
    }
}
