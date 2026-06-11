package app.chiggi.sonyliv.patches.geo

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val suppressGeoBlockPatch = bytecodePatch(
    name = "Suppress geo / VPN block",
    description = "Stops the dedicated \"not available in your region\" and \"VPN detected\" " +
        "block screens by downgrading the geo/EPD block to a generic error. " +
        "Geoblocking is enforced server-side by IP, so this does not grant access without a " +
        "valid in-region IP (e.g. a working India connection).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // Always return LocationGenericException instead of the Geo/EPD blocked variants.
        // p0 = this, p1 = ApiResult.Error.
        GetLocationExceptionFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/sonyliv/repository/LocationException${'$'}LocationGenericException;
                invoke-direct { v0, p1 }, Lcom/sonyliv/repository/LocationException${'$'}LocationGenericException;-><init>(Lcom/sonyliv/network/model/ApiResult${'$'}Error;)V
                return-object v0
            """,
        )
    }
}
