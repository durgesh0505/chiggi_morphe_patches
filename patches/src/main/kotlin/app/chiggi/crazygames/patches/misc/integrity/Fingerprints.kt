package app.chiggi.crazygames.patches.misc.integrity

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.pairip.licensecheck.LicenseClient — the license-check core of Google Play's "Automatic
 * Integrity Protection" (PairIP), injected at Play-signing time. It verifies, via Play licensing,
 * that the app was acquired from Play and signed with the Play key; on a re-signed sideload it fails
 * and launches LicenseActivity (the "get it on Google Play" gate that redirects to the store).
 *
 * The package/class/method names are fixed (Google-injected, not app R8-obfuscated), so these are
 * stable anchors. There are two entry points, both funneling into the same check:
 *   - checkLicense(Context)        — static; called from com.pairip.application.Application.attachBaseContext
 *   - initializeLicenseCheck()     — instance; called from LicenseContentProvider.onCreate
 * Neutering both to return-void stops the gate while leaving the PairIP Application wrapper (which
 * boots the real app) intact. There is no VMRunner/code-virtualization in this build, so nothing
 * decrypts code that depends on the check.
 */
private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"

internal object CheckLicenseFingerprint : Fingerprint(
    name = "checkLicense",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    definingClass = LICENSE_CLIENT,
)

internal object InitializeLicenseCheckFingerprint : Fingerprint(
    name = "initializeLicenseCheck",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
    definingClass = LICENSE_CLIENT,
)
