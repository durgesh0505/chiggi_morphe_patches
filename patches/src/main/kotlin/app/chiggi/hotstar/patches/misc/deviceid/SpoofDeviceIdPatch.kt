package app.chiggi.hotstar.patches.misc.deviceid

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val spoofDeviceIdPatch = bytecodePatch(
    name = "Spoof device id",
    description = "Replaces the device id JioHotstar derives from Settings.Secure ANDROID_ID with a " +
        "stable-per-install random UUID, so the re-signed build presents as a device the server has " +
        "never flagged. This is the verified single source of both the X-HS-Device-Id request header " +
        "and the /v2/start body device ids; ANDROID_ID otherwise survives an app-data clear (it is " +
        "keyed to the signing key), which is why a flagged device kept getting the \"device logged " +
        "out / suspicious activity\" page on every launch. The id is persisted, and regenerates on an " +
        "app-data clear so a re-flagged device can be rotated. NOTE: the server also sees the " +
        "connection IP — pair this with a clean (ideally residential) India IP; a flagged exit IP is " +
        "not something the client can change. Do not run on an account you cannot afford to lose.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)
    extendWith("extensions/extension.mpe")

    execute {
        // Return the spoofed id immediately (the suspend getter resolves synchronously with it), so
        // every X-HS-Device-Id header and every /v2/start device-id entry uses the fresh id.
        DeviceIdGetterFingerprint.method.addInstructions(0, """
            invoke-static {}, Lapp/chiggi/hotstar/extension/HsDeviceIdSpoof;->deviceId()Ljava/lang/String;
            move-result-object v0
            return-object v0
        """)
    }
}
