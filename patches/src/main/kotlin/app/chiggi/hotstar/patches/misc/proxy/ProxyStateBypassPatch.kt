package app.chiggi.hotstar.patches.misc.proxy

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

// Return kotlin.Unit (R8-renamed to Lcom/... = Pj.e; Pj.e.a is Unit.INSTANCE) immediately, so the
// coroutine completes without running the store-write logic. The verdict is never persisted.
private val RETURN_UNIT = """
    sget-object v0, LPj/e;->a:LPj/e;
    return-object v0
"""

@Suppress("unused")
val proxyStateBypassPatch = bytecodePatch(
    name = "Bypass proxy/VPN security block",
    description = "Stops the \"Something is interfering with your secure connection\" (NET_201) " +
        "screen that JioHotstar shows when it detects a VPN/proxy. The app self-enforces a proxy " +
        "verdict the server sends via the X-Hs-SetProxyState response header: the client stores it " +
        "and, when it is BLOCKED/RESTRICTED, shows the security error. This neuters the handler that " +
        "stores that verdict, so the client keeps reporting a clean (unblocked) proxy state and " +
        "never blocks itself. It also neuters the store's periodic proxy-state refresh coroutines, " +
        "which would otherwise re-fetch endlessly (the verdict is never stored, so the TTL check " +
        "always sees it expired) and make the app feel laggy/retrying. Note: if JioHotstar also " +
        "refuses playback server-side based on the connection IP, video may still fail even though " +
        "the error screen is gone.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        // Never store the server's BLOCKED verdict -> client stays unblocked, no NET_201.
        ProxyStateVerdictHandlerFingerprint.method.addInstructions(0, RETURN_UNIT)

        // Stop the endless periodic re-fetch caused by the never-stored (always-expired) state.
        EnrichRefreshmentFingerprint.method.addInstructions(0, RETURN_UNIT)
        LocationRefreshmentFingerprint.method.addInstructions(0, RETURN_UNIT)
    }
}
