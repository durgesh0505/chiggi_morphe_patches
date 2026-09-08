package app.chiggi.googlephone.patches.callscreen

import app.morphe.patcher.Fingerprint

/**
 * The Call Screen ("call assist") availability gate: com.android.dialer.dobby.enabledfn.DobbyEnabledFn
 * .isEnabled() (obfuscated to defpackage.jzl.a() in v235.0). Returns true iff any of ~15 revelio/dobby
 * Phenotype flags is on, else logs "disabled by flag" and returns false (also false during direct boot).
 * Forcing it true makes the Call Screen entry appear.
 *
 * Anchored on the surviving zqz log-path literal + the "disabled by flag" decision string, plus
 * returnType Z / no params — the obfuscated class name is not stable across builds.
 */
internal object DobbyEnabledFnFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    strings = listOf(
        "com/android/dialer/dobby/enabledfn/DobbyEnabledFn",
        "disabled by flag",
    ),
)
