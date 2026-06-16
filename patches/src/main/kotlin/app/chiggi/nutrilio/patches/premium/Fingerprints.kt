package app.chiggi.nutrilio.patches.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/**
 * The PremiumModule's master gate is a single getter that reads the locally stored "Plus active"
 * flag:
 *
 *   public final boolean t2() { return ((Boolean) kj.g.d(kj.g.E)).booleanValue(); }
 *
 * It is the only `public final ()Z` method on the PremiumModule class and is called from ~19 sites
 * (paywall, premium colors, charts, app lock, all tracking options). Forcing it to return true
 * unlocks Nutrilio PLUS+ regardless of billing/server receipt state.
 *
 * Rather than pinning the obfuscated name (`yk.z.t2`, which changes per release) we anchor on the
 * class: the PremiumModule emits the backend event "p_be_premium_restored", a stable string. This
 * keeps the fingerprint working across versions as long as that event name and the getter signature
 * survive.
 */
private const val PREMIUM_MODULE_ANCHOR_STRING = "p_be_premium_restored"

internal object PremiumModuleIsPlusActiveFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    custom = { _, classDef ->
        classDef.methods.any { method ->
            method.implementation?.instructions?.any { instruction ->
                instruction is ReferenceInstruction &&
                    (instruction.reference as? StringReference)?.string == PREMIUM_MODULE_ANCHOR_STRING
            } == true
        }
    },
)
