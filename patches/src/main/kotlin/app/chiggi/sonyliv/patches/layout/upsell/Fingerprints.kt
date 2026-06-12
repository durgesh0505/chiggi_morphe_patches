package app.chiggi.sonyliv.patches.layout.upsell

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val CT_NATIVE_DISPLAY_REPO =
    "Lcom/sonyliv/data/clevertap/CtNativeDisplayRepositoryImpl;"

/**
 * com.sonyliv.data.clevertap.CtNativeDisplayRepositoryImpl#getCTListRowData(String): CTListRowModel
 *
 * Returns the CleverTap native-display promo *tray* model for the home screen (or null when none).
 * Forcing null means the home screen never renders a CleverTap promo tray. Overlays/pop-ups use a
 * separate path and are unaffected.
 */
internal object GetCTListRowDataFingerprint : Fingerprint(
    name = "getCTListRowData",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Lcom/sonyliv/domain/clevertap/model/CTListRowModel;",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = CT_NATIVE_DISPLAY_REPO,
)

/**
 * com.sonyliv.data.clevertap.CtNativeDisplayRepositoryImpl#getCTRowData(String): CTBannerCardRowModel
 *
 * Returns the CleverTap native-display promo *banner card* model for the home screen (or null).
 * Forcing null hides the promo/subscribe banner on home.
 */
internal object GetCTBannerRowDataFingerprint : Fingerprint(
    name = "getCTRowData",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Lcom/sonyliv/domain/clevertap/model/CTBannerCardRowModel;",
    parameters = listOf("Ljava/lang/String;"),
    definingClass = CT_NATIVE_DISPLAY_REPO,
)
