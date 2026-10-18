package app.chiggi.zombiecatcher.patches.license

import app.chiggi.zombiecatcher.patches.shared.Constants.COMPATIBILITY_ZOMBIE_CATCHERS
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private const val STARTUP_PROVIDER = "androidx.startup.InitializationProvider"
private val BROKEN_INITIALIZERS = setOf(
    "androidx.work.WorkManagerInitializer",
    "androidx.profileinstaller.ProfileInstallerInitializer",
)

// PairIP normally initializes obfuscated String fields before these AndroidX components run. The
// encrypted startup program cannot run after re-signing. WorkManager then passes a null Room column
// name, while ProfileInstaller passes a null key to System.getProperty; both terminate the process.
// Game notifications and profile optimization are optional. Notification entrypoints are also
// disabled by Bypass license check.
internal val disableBrokenWorkManagerPatch = resourcePatch {
    compatibleWith(COMPATIBILITY_ZOMBIE_CATCHERS)

    execute {
        document("AndroidManifest.xml").use { document ->
            val providers = document.getElementsByTagName("provider")
            val removed = LinkedHashSet<String>()
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as Element
                if (provider.getAttribute("android:name") != STARTUP_PROVIDER) continue

                val children = provider.childNodes
                val matches = ArrayList<Element>()
                for (j in 0 until children.length) {
                    val child = children.item(j)
                    if (child is Element && child.tagName == "meta-data") {
                        val name = child.getAttribute("android:name")
                        if (name in BROKEN_INITIALIZERS) {
                            matches.add(child)
                            removed.add(name)
                        }
                    }
                }
                matches.forEach(provider::removeChild)
            }
            if (removed != BROKEN_INITIALIZERS) {
                throw PatchException(
                    "Expected startup initializers $BROKEN_INITIALIZERS, removed $removed",
                )
            }
        }
    }
}
