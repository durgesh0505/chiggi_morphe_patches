package app.chiggi.zombiecatcher.patches.misc.packagename

import app.chiggi.zombiecatcher.patches.shared.Constants.COMPATIBILITY_ZOMBIE_CATCHERS
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE = "fi.twomenandadog.zombiecatchers"

@Suppress("unused")
val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Changes the manifest package and package-derived authorities/permissions so " +
        "Zombie Catchers Morphe can install beside the original. Play Games, Firebase auth, store " +
        "updates, and server checks can still reject the renamed and re-signed build.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZOMBIE_CATCHERS)

    val packageName by stringOption(
        key = "packageName",
        default = "$ORIGINAL_PACKAGE.morphe",
        title = "Package name",
        description = "New package name for side-by-side installation.",
        required = true,
    ) {
        it != null && it.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$"))
    }

    finalize {
        document("AndroidManifest.xml").use { document ->
            val newPackage = packageName!!
            document.documentElement.setAttribute("package", newPackage)

            val providers = document.getElementsByTagName("provider")
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as Element
                val authorities = provider.getAttribute("android:authorities")
                if (authorities.startsWith("$ORIGINAL_PACKAGE.")) {
                    provider.setAttribute(
                        "android:authorities",
                        authorities.replaceFirst(ORIGINAL_PACKAGE, newPackage),
                    )
                }
            }

            listOf("permission", "uses-permission").forEach { tag ->
                val nodes = document.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    val name = node.getAttribute("android:name")
                    if (name.startsWith("$ORIGINAL_PACKAGE.")) {
                        node.setAttribute(
                            "android:name",
                            name.replaceFirst("$ORIGINAL_PACKAGE.", "$newPackage."),
                        )
                    }
                }
            }
        }
    }
}
