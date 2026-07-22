package app.chiggi.ayababy.patches.premium

import app.morphe.patcher.Fingerprint

/**
 * com.apphud.sdk.Apphud — the Apphud subscription SDK singleton. The app decides premium via
 * Apphud.hasPremiumAccess() (which itself calls hasActiveSubscription() first, then checks
 * non-renewing purchases). Forcing both to true makes every entitlement check report premium with no
 * purchase. Apphud class/method names are not obfuscated, so these anchor on definingClass + name.
 */
internal object HasPremiumAccessFingerprint : Fingerprint(
    definingClass = "Lcom/apphud/sdk/Apphud;",
    name = "hasPremiumAccess",
    returnType = "Z",
)

internal object HasActiveSubscriptionFingerprint : Fingerprint(
    definingClass = "Lcom/apphud/sdk/Apphud;",
    name = "hasActiveSubscription",
    returnType = "Z",
)
