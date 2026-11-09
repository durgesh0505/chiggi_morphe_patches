package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption

private val PACKAGE_BOUND_NAME_TAGS = setOf("permission", "uses-permission", "uses-permission-sdk-23", "action")

private fun shouldRewritePackageBoundAttribute(elementName: String, attributeName: String): Boolean {
    val localName = attributeName.substringAfter(':')
    return when (localName) {
        "authorities", "host", "permission", "readPermission", "scheme", "targetPackage", "targetProcesses", "taskAffinity", "writePermission" -> true
        "name" -> elementName in PACKAGE_BOUND_NAME_TAGS
        else -> false
    }
}

@Suppress("unused")
val capcutChangePackageNamePatch = resourcePatch(
    name = "Change Package Name",
    description = "Changes CapCut's manifest package and package-bound authorities, permissions, actions, schemes, hosts, and instrumentation targets for side-by-side install. Login, deep links, and server identity may break.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    val packageName by stringOption(key = "packageName", default = "com.lemon.lvoverseas.morphe", title = "Package name", required = true)
    finalize {
        document("AndroidManifest.xml").use { document ->
            val old = Constants.CAPCUT_PACKAGE_NAME
            val new = packageName!!
            document.documentElement.setAttribute("package", new)

            val elements = document.getElementsByTagName("*")
            for (i in 0 until elements.length) {
                val element = elements.item(i)
                val attributes = element.attributes ?: continue
                for (attributeIndex in 0 until attributes.length) {
                    val attribute = attributes.item(attributeIndex)
                    if (old in attribute.nodeValue && shouldRewritePackageBoundAttribute(element.nodeName, attribute.nodeName)) {
                        attribute.nodeValue = attribute.nodeValue.replace(old, new)
                    }
                }
            }
        }
    }
}
