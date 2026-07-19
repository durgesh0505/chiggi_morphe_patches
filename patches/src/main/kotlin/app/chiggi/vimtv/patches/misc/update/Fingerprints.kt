package app.chiggi.vimtv.patches.misc.update

import app.morphe.patcher.Fingerprint

/**
 * com.yupptv.ottsdk.model.LocationInfo$ClientInfo#getVersionNumber()
 *
 * The "update available" popup (both the forced/non-cancelable and the optional variant) is gated on
 * this value. SplashActivity: `if (parseInt(clientInfo.getVersionNumber()) <= 34) return false;`
 * (no popup); MainActivity: `if (parseInt(clientInfo.getVersionNumber()) > 34 &&
 * getUpdateType() == 1) { forced popup }`. Forcing this getter to "0" makes 0 <= 34 true in
 * SplashActivity (returns before showing anything) and 0 > 34 false in MainActivity (skips the
 * forced block and short-circuits before getUpdateType(), so no NPE). It is used in exactly these
 * two update checks, so "0" disables the update nag with no other effect. Returning a String keeps
 * parseInt safe. Real class names are kept; ClientInfo is an inner class of LocationInfo.
 */
internal object GetVersionNumberFingerprint : Fingerprint(
    definingClass = "Lcom/yupptv/ottsdk/model/LocationInfo\$ClientInfo;",
    name = "getVersionNumber",
    returnType = "Ljava/lang/String;",
)
