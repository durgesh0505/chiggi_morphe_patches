package app.chiggi.bighunter.patches.license

import app.morphe.patcher.Fingerprint

// PairIP (Google Play integrity/license wrapper) — com.pairip.licensecheck.LicenseClient. On a re-signed
// sideload the installer/licensing check fails (NOT_LICENSED) and the app shows "get this game from Play"
// (startPaywallActivity) or an error dialog, then System.exit. Neuter the entry + both block paths so the
// operator's own re-signed offline build runs. Classes unobfuscated -> fingerprint on definingClass + name.

internal object InitializeLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    parameters = listOf(),
)

internal object StartPaywallActivityFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startPaywallActivity",
    returnType = "V",
    parameters = listOf("Landroid/app/PendingIntent;"),
)

internal object StartErrorDialogActivityFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startErrorDialogActivity",
    returnType = "V",
    parameters = listOf(),
)
