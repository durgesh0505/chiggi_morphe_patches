package app.chiggi.foxone.patches.pairip

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.pairip.licensecheck.LicenseClient#checkLicense(Context)
 *
 * Entry point of the PairIP (Google Play Automatic Integrity Protection) license check, run at
 * startup by LicenseContentProvider. On failure it shows the "Something went wrong / check Google
 * Play" dialog and kills the app. PairIP keeps its own class names (not obfuscated).
 */
internal object PairipCheckLicenseFingerprint : Fingerprint(
    name = "checkLicense",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
)
