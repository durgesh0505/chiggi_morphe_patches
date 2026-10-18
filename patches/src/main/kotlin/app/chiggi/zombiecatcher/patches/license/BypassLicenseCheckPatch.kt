package app.chiggi.zombiecatcher.patches.license

import app.chiggi.zombiecatcher.patches.shared.Constants.COMPATIBILITY_ZOMBIE_CATCHERS
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

private val RETURN_TRUE = """
    const/4 v0, 0x1
    return v0
"""

@Suppress("unused")
val bypassLicenseCheckPatch = bytecodePatch(
    name = "Bypass license check",
    description = "Bypasses PairIP signature and Google Play license checks. It also bypasses " +
        "PairIP's encrypted-key import bootstrap, neutralizes its protected native logger, " +
        "restores the game library's PairIP-encrypted native data, " +
        "disables broken AndroidX startup and Firebase anonymous auth, and reconstructs the " +
        "critical " +
        "application, activity, and notification-service lifecycle methods so a re-signed build " +
        "can start without encrypted VM bytecode. Third-party SDK VM stubs remain a runtime risk.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZOMBIE_CATCHERS)
    dependsOn(disableProtectedLoggerPatch)
    dependsOn(restoreNativeDataPatch)
    dependsOn(disableBrokenWorkManagerPatch)

    execute {
        listOf(
            VerifyIntegrityFingerprint,
            CheckLicenseFingerprint,
            InitializeLicenseCheckFingerprint,
            StartPaywallFingerprint,
            StartErrorDialogFingerprint,
            StartupLauncherFingerprint,
            ScheduleNotificationFingerprint,
            CancelAllNotificationsFingerprint,
            SignInAnonymouslyFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }

        KeyImportApplicationHasKeyFingerprint.method.addInstructions(0, RETURN_TRUE)
        KeyImportFactoryHasKeyFingerprint.method.addInstructions(0, RETURN_TRUE)

        val activityClass = mutableClassDefBy(
            "Lfi/twomenandadog/zombiecatchers/ZCActivity;",
        )
        if (activityClass.methods.none {
                it.name == "getDeviceId" && it.parameterTypes.isEmpty() &&
                    it.returnType == "Ljava/lang/String;"
            }
        ) {
            activityClass.methods.add(
                MutableMethod(RetrieveDeviceIdFingerprint.method).apply {
                    name = "getDeviceId"
                },
            )
        }

        ApplicationOnCreateFingerprint.method.addInstructions(0, """
            invoke-super {p0}, Landroid/app/Application;->onCreate()V
            invoke-direct {p0}, Lfi/twomenandadog/zombiecatchers/ZombieCatchersApp;->createNotificationChannel()V
            invoke-direct {p0}, Lfi/twomenandadog/zombiecatchers/ZombieCatchersApp;->checkIfAppIsAvailableOnStore()V
            return-void
        """)
        ActivityOnCreateFingerprint.method.addInstructions(0, """
            invoke-super {p0, p1}, Lfi/twomenandadog/zombiecatchers/ZCActivity;->onCreate(Landroid/os/Bundle;)V
            return-void
        """)
        ActivityOnPauseFingerprint.method.addInstructions(0, """
            invoke-super {p0}, Lfi/twomenandadog/zombiecatchers/ZCActivity;->onPause()V
            return-void
        """)
        ActivityOnResumeFingerprint.method.addInstructions(0, """
            invoke-super {p0}, Lfi/twomenandadog/zombiecatchers/ZCActivity;->onResume()V
            return-void
        """)

        NotificationOnBindFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)
        NotificationOnDestroyFingerprint.method.addInstructions(0, """
            invoke-super {p0}, Landroid/app/Service;->onDestroy()V
            return-void
        """)
        NotificationOnStartCommandFingerprint.method.addInstructions(0, """
            invoke-direct {p0, p1}, Lfi/twomenandadog/zombiecatchers/ZCNotificationService;->handleIntent(Landroid/content/Intent;)V
            const/4 v0, 0x2
            return v0
        """)
    }
}
