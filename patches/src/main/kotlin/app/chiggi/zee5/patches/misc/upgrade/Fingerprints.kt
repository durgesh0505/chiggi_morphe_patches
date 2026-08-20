package app.chiggi.zee5.patches.misc.upgrade

import app.morphe.patcher.Fingerprint

/**
 * The only hard client-side block screen on ZEE5 is the forced-upgrade blocker (verified: no geo/VPN/
 * tamper gate exists — root/emulator/attestation flags are telemetry only). BlockerScreenInfo.isBlocked()
 * decides whether the app renders the block/upgrade wall; forcing it false lets the app continue.
 */
internal object IsBlockedFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/launch/domain/BlockerScreenInfo;",
    name = "isBlocked",
    returnType = "Z",
)
