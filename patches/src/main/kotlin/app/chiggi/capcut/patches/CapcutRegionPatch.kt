package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val FLAVOR_LOCALE = "Lcom/vega/core/utils/FlavorLocale;"
private fun flavor(name: String, params: List<String> = listOf()) = Fingerprint(
    definingClass = FLAVOR_LOCALE, name = name, returnType = "Ljava/lang/String;", parameters = params,
)
private const val CARRIER = "Lcom/bytedance/bpea/tt/entry/api/device/info/CarrierEntry\$Companion;"
private val TELEPHONY = listOf("Landroid/telephony/TelephonyManager;")
private val TELEPHONY_CERT = listOf("Landroid/telephony/TelephonyManager;", "Lcom/bytedance/bpea/basics/Cert;")
private fun carrier(name: String, params: List<String>) = Fingerprint(
    definingClass = CARRIER, name = name, returnType = "Ljava/lang/String;", parameters = params,
)
private object LocaleCountry : Fingerprint(
    definingClass = "Lcom/bytedance/bpea/tt/entry/api/device/info/LocaleEntry\$Companion;",
    name = "b", returnType = "Ljava/lang/String;", parameters = listOf("Ljava/util/Locale;"),
)

@Suppress("unused")
val capcutSpoofRegionPatch = bytecodePatch(
    name = "Spoof Region to USA",
    description = "Reports US app, carrier, SIM, network and locale regions. Use with a US VPN; the server still sees the source IP. Does not alter stateful store_region echoes.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute {
        listOf(
            flavor("b"), flavor("c", listOf("Landroid/content/Context;")),
            flavor("d", listOf("Landroid/content/Context;")), flavor("e", listOf("Landroid/content/Context;")),
            flavor("g"), flavor("k"), LocaleCountry,
        ).forEach { it.method.addInstructions(0, "const-string v0, \"US\"\nreturn-object v0") }
        listOf(
            carrier("d", TELEPHONY_CERT), carrier("e", TELEPHONY),
            carrier("j", TELEPHONY_CERT), carrier("k", TELEPHONY),
        ).forEach { it.method.addInstructions(0, "const-string v0, \"us\"\nreturn-object v0") }
        listOf(
            carrier("f", TELEPHONY_CERT), carrier("g", TELEPHONY),
            carrier("l", TELEPHONY_CERT), carrier("m", TELEPHONY),
        ).forEach { it.method.addInstructions(0, "const-string v0, \"310260\"\nreturn-object v0") }
        listOf(
            carrier("h", TELEPHONY_CERT), carrier("i", TELEPHONY),
            carrier("n", TELEPHONY_CERT), carrier("o", TELEPHONY),
        ).forEach { it.method.addInstructions(0, "const-string v0, \"T-Mobile\"\nreturn-object v0") }
    }
}
