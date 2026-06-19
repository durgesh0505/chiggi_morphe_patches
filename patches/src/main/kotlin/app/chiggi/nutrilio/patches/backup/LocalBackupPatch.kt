package app.chiggi.nutrilio.patches.backup

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val localBackupRestorePatch = bytecodePatch(
    name = "Local backup & restore",
    description = "Adds a native \"Local backup & restore\" row to the Backup screen (above Restore " +
        "Backup) that opens Nutrilio's built-in (but normally hidden) local file backup/restore. " +
        "There you can export a complete backup — all entries, images and settings — to a portable " +
        ".nutrilio file (a ZIP) via the share sheet, and restore one from a file you pick. No " +
        "Google account, no premium needed. (Google Drive auto-backup is separate and still " +
        "requires sign-in, which does not work on a re-signed app.)",
    default = true,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

    // Pulls in the BackupLauncher extension class.
    extendWith("extensions/extension.mpe")

    execute {
        // BackupActivity.onCreate(Bundle): p0 = this. Insert the themed "Local backup & restore" row
        // above the Restore Backup row. install() posts to the decor view, so running at index 0
        // (before setContentView) is safe — the row is added once the layout is ready.
        BackupActivityOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static { p0 }, " +
                "Lapp/chiggi/nutrilio/extension/BackupLauncher;->install(Landroid/app/Activity;)V",
        )
    }
}
