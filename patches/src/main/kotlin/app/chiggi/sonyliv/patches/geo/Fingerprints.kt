package app.chiggi.sonyliv.patches.geo

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.sonyliv.repository.LocationRepositoryImpl
 *
 * #getLocationException(ApiResult.Error error): LocationException
 * Maps a failed location-API response to a block: when the server's error message contains
 * "Geoblocked" it returns LocationGeoBlockedException, "EPDblocked" -> LocationEpdBlockedException,
 * otherwise LocationGenericException. We force it to always return the generic exception so the
 * dedicated geo / VPN block screens are not shown.
 *
 * NOTE: geoblocking is enforced server-side by IP; this only suppresses the client block screens,
 * it does not grant access without a valid in-region IP.
 */
internal object GetLocationExceptionFingerprint : Fingerprint(
    name = "getLocationException",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "Lcom/sonyliv/repository/LocationException;",
    parameters = listOf("Lcom/sonyliv/network/model/ApiResult${'$'}Error;"),
    definingClass = "Lcom/sonyliv/repository/LocationRepositoryImpl;",
)
