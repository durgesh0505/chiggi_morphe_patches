package app.chiggi.vimtv.patches.ad

import app.morphe.patcher.Fingerprint

/**
 * Vi Movies and TV (YuppTV) ad hooks. Ads are URL-driven: the player builds its IMA/SSAI ad-tag URI
 * from AdUrlResponse.getAdUrlTypes(), and every ad-load path first checks the list is non-null and
 * non-empty. Forcing that getter to return null removes both client IMA (CSAI) pre/mid-roll and SSAI
 * ad tags at the source. TorcAiAdConfig.isAdEnabled() is the higher ad-enable gate. Real class names
 * are kept (light obfuscation), so these anchor on definingClass + method name.
 */
internal object GetAdUrlTypesFingerprint : Fingerprint(
    definingClass = "Lcom/yupptv/ottsdk/model/ads/AdUrlResponse;",
    name = "getAdUrlTypes",
    returnType = "Ljava/util/List;",
)

internal object IsAdEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/yupptv/ottsdk/model/user/TorcAiAdConfig;",
    name = "isAdEnabled",
    returnType = "Z",
)
