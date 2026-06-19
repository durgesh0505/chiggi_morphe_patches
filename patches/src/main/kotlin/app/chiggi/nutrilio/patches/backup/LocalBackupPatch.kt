package app.chiggi.nutrilio.patches.backup

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val localBackupRestorePatch = bytecodePatch(
    name = "Local backup & restore",
    description = "Adds native \"Export backup file\" and \"Import backup file\" rows to the Backup " +
        "screen (above Restore Backup) that drive Nutrilio's built-in (but normally hidden) local " +
        "file backup/restore. Exports a complete backup — all entries, images and settings — to a " +
        "portable .nutrilio file (a ZIP) via the share sheet, and restores one from a file you " +
        "pick. No Google account, no premium needed. (Google Drive auto-backup is separate and " +
        "still requires sign-in, which does not work on a re-signed app.)",
    default = true,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

    // Pulls in the BackupLauncher extension class.
    extendWith("extensions/extension.mpe")

    execute {
        // BackupActivity.onCreate(Bundle): p0 = this. Insert the two themed Export/Import rows above
        // the Restore Backup row. install() posts to the decor view, so running at index 0 (before
        // setContentView) is safe — the rows are added once the layout is ready.
        BackupActivityOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static { p0 }, " +
                "Lapp/chiggi/nutrilio/extension/BackupLauncher;->install(Landroid/app/Activity;)V",
        )

        // DebugBackupActivity.onCreate(Bundle): p0 = this. When launched with the morphe_backup_action
        // extra, auto-click the app's own export/import button so its real backup machinery runs.
        DebugBackupActivityOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static { p0 }, " +
                "Lapp/chiggi/nutrilio/extension/BackupLauncher;->autoAction(Landroid/app/Activity;)V",
        )
    }
}
