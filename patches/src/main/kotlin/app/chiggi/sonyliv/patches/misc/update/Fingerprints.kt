package app.chiggi.sonyliv.patches.misc.update

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.sonyliv.ui.home.AppUpgradeUseCaseKt#getAppUpgradeData()
 *
 * Returns the Bundle that drives the "update available" popup (immediate/forced or flexi),
 * or null when no update popup should be shown. Class name is not obfuscated in 6.23.1.
 */
internal object AppUpgradeDataFingerprint : Fingerprint(
    name = "getAppUpgradeData",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Landroid/os/Bundle;",
    parameters = listOf(),
    definingClass = "Lcom/sonyliv/ui/home/AppUpgradeUseCaseKt;",
)
