package app.chiggi.threads.patches.ads

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

/**
 * Hook 1 — the canonical "is this media an ad/sponsored" boolean.
 *
 *   // Lcom/instagram/feed/media/Media;
 *   public final boolean DED() {
 *       return X.9A5.A00(new X.9A4(this.A04));
 *   }
 *
 * `Media.DED()Z` is a pure predicate (no side effects) routed through the single ad-classification
 * chokepoint `LX/9A5;->A00(LX/9A4;)Z`, and is consumed by every feed/clip surface to decide whether
 * to render ad chrome (CTA bar, "Sponsored" label, click-through). Forcing it to return false makes
 * every media report as organic, stripping ad treatment.
 *
 * Anchored on the NAMED class `com.instagram.feed.media.Media` (stable across versions). The method
 * name `DED` is R8-obfuscated and pinned to 434.0.0.41.74; re-verify on app updates.
 */
internal object MediaIsAdFingerprint : Fingerprint(
    name = "DED",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    definingClass = "Lcom/instagram/feed/media/Media;",
)

/**
 * Hook 2 — the Threads feed ad-injection scheduler (true removal, feed-scoped).
 *
 *   // Lcom/instagram/barcelona/feed/data/cache/BarcelonaSpoolFeedCacheHandler;
 *   public final boolean A0g(X.3oS item, int position) {
 *       ... new BarcelonaSpoolFeedCacheHandler$injectAdToFeedSessionAtPosition$1(...) ...
 *   }
 *
 * This is the public entry point that schedules insertion of an ad feed-item (`X.3oS`) into the live
 * feed session at a position. Returning false short-circuits before the async injector is posted, so
 * the ad slot is never created (no blank card). The return value already signals "did we inject",
 * which callers tolerate.
 *
 * Anchored on the NAMED class `BarcelonaSpoolFeedCacheHandler` plus the descriptive, stable inner
 * lambda class name `injectAdToFeedSessionAtPosition$1` (kept by Kotlin), rather than the obfuscated
 * method name `A0g`, so it survives method-name reshuffling.
 */
private const val FEED_CACHE_HANDLER_CLASS =
    "Lcom/instagram/barcelona/feed/data/cache/BarcelonaSpoolFeedCacheHandler;"
private const val INJECT_AD_ANCHOR = "injectAdToFeedSessionAtPosition"

internal object InjectAdToFeedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    definingClass = FEED_CACHE_HANDLER_CLASS,
    custom = { method, _ ->
        method.implementation?.instructions?.any { instruction ->
            instruction is ReferenceInstruction &&
                (instruction.reference as? TypeReference)?.type?.contains(INJECT_AD_ANCHOR) == true
        } == true
    },
)
