package app.chiggi.foxone.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_FOXONE = Compatibility(
        name = "FOX One",
        packageName = "com.fox.foxone",
        // Distributed as split APKs.
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0C2340, // FOX navy
        targets = listOf(
            AppTarget(
                version = "1.17.1",
                minSdk = 24,
            ),
            AppTarget(
                version = "1.15.1",
                minSdk = 24,
            ),
        ),
    )
}
