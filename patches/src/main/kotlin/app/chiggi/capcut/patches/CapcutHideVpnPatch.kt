package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private object NetworkUtilsVpn : Fingerprint(definingClass = "Lcom/vega/infrastructure/util/NetworkUtils;", name = "g", returnType = "Z", parameters = listOf())
private object CronetVpn : Fingerprint(definingClass = "Lcom/ttnet/org/chromium/net/AndroidNetworkLibrary;", name = "checkIsVpnOn", returnType = "Z", parameters = listOf())
private object WifiProxy : Fingerprint(definingClass = "Lcom/bytedance/common/utility/DeviceUtils;", name = "isWifiProxy", returnType = "Z", parameters = listOf("Landroid/content/Context;"))

@Suppress("unused")
val capcutHideVpnPatch = bytecodePatch(
    name = "Hide VPN and Proxy Flags",
    description = "Reports VPN/proxy flags false without touching TTNet VPN routing or network IDs. Server IP reputation remains visible.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    execute { listOf(NetworkUtilsVpn, CronetVpn, WifiProxy).forEach { it.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0") } }
}
