package app.chiggi.vimtv.patches.premium

import app.morphe.patcher.Fingerprint

/**
 * Client-side "subscribed" flags. Vi Movies and TV gates content server-side (entitlement + Widevine
 * DRM), so forcing these true only changes UI state — it does not unlock content you are not
 * entitled to, and may make some content appear playable when the server will still refuse it.
 */
internal object ContentInfoIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/yupptv/ottsdk/model/user/UserAuthSSO\$ContentInfo;",
    name = "isSubscribed",
    returnType = "Z",
)

internal object PackageIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/yupptv/ottsdk/model/payments/packagefeature/Package;",
    name = "getIsSubscribed",
    returnType = "Ljava/lang/Boolean;",
)
