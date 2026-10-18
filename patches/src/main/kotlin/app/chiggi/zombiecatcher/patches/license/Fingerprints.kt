package app.chiggi.zombiecatcher.patches.license

import app.morphe.patcher.Fingerprint

internal object VerifyIntegrityFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/SignatureCheck;",
    name = "verifyIntegrity",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal object CheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal object InitializeLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    parameters = listOf(),
)

internal object StartPaywallFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startPaywallActivity",
    returnType = "V",
    parameters = listOf("Landroid/app/PendingIntent;"),
)

internal object StartErrorDialogFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startErrorDialogActivity",
    returnType = "V",
    parameters = listOf(),
)

internal object StartupLauncherFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/StartupLauncher;",
    name = "launch",
    returnType = "V",
    parameters = listOf(),
)

internal object KeyImportApplicationHasKeyFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/vmencryption/KeyImportApplication;",
    name = "isEncryptionKeyPresent",
    returnType = "Z",
    parameters = listOf(),
)

internal object KeyImportFactoryHasKeyFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/vmencryption/KeyImportAppComponentFactory;",
    name = "isEncryptionKeyPresent",
    returnType = "Z",
    parameters = listOf(),
)

internal object ApplicationOnCreateFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZombieCatchersApp;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
)

internal object ActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCGoogleAcitivty;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

internal object ActivityOnPauseFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCGoogleAcitivty;",
    name = "onPause",
    returnType = "V",
    parameters = listOf(),
)

internal object ActivityOnResumeFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCGoogleAcitivty;",
    name = "onResume",
    returnType = "V",
    parameters = listOf(),
)

internal object NotificationOnBindFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCNotificationService;",
    name = "onBind",
    returnType = "Landroid/os/IBinder;",
    parameters = listOf("Landroid/content/Intent;"),
)

internal object NotificationOnDestroyFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCNotificationService;",
    name = "onDestroy",
    returnType = "V",
    parameters = listOf(),
)

internal object NotificationOnStartCommandFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCNotificationService;",
    name = "onStartCommand",
    returnType = "I",
    parameters = listOf("Landroid/content/Intent;", "I", "I"),
)

internal object ScheduleNotificationFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCActivity;",
    name = "scheduleNotification",
    returnType = "V",
    parameters = listOf(
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "I",
    ),
)

internal object CancelAllNotificationsFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCActivity;",
    name = "cancelAllNotifications",
    returnType = "V",
    parameters = listOf(),
)

internal object SignInAnonymouslyFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCActivity;",
    name = "signInAnonymously",
    returnType = "V",
    parameters = listOf(),
)

internal object RetrieveDeviceIdFingerprint : Fingerprint(
    definingClass = "Lfi/twomenandadog/zombiecatchers/ZCActivity;",
    name = "retrieveDeviceId",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)
