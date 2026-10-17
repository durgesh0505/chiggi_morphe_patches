package app.chiggi.zombiecatcher.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_ZOMBIE_CATCHERS = Compatibility(
        name = "Zombie Catchers",
        packageName = "fi.twomenandadog.zombiecatchers",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x7DBA45,
        targets = listOf(
            AppTarget(
                version = "1.69.16",
                minSdk = 24,
            ),
        ),
    )
}
