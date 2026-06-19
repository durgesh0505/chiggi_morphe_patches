package app.chiggi.nutrilio.patches.backup

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * net.nutrilio.view.activities.backup.BackupActivity#onCreate(Bundle) — the (Google Drive) backup
 * screen the user reaches from settings. Hooking its onCreate is the entry point to inject a button
 * that opens the app's hidden local-file backup/restore screen (DebugBackupActivity).
 *
 * NAMED class (not obfuscated) => version-robust; the method is public final onCreate(Bundle).
 */
internal object BackupActivityOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    definingClass = "Lnet/nutrilio/view/activities/backup/BackupActivity;",
)
