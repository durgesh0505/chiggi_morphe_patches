package app.chiggi.threads.patches.ads

import app.chiggi.threads.patches.shared.Constants.COMPATIBILITY_THREADS
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Hides sponsored / "netego" units from the Threads feed using two complementary hooks:
 *
 *  1. Media.DED()Z -> false : every media reports as organic, stripping ad chrome (the "Sponsored"
 *     label, CTA bar, click-through). Covers server-inlined ads that arrive already in the response.
 *  2. BarcelonaSpoolFeedCacheHandler.A0g(...)Z -> false : blocks the client-side ad-injection
 *     scheduler so injected ad slots are never created (true removal, no blank card).
 *
 * Together they cover both ad delivery paths (inline + injected). Client-side only; Threads still
 * fetches ads from the server, the patch just refuses to surface them.
 *
 * NOT device-verified by the author — only bytecode application is confirmed. Forcing DED() false is
 * global (also affects non-feed surfaces such as reels); the injection hook is feed-scoped. If a
 * future build desyncs feed position counters, re-verify on device.
 */
@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides sponsored posts and injected \"suggested\" netego units from the Threads " +
        "feed. Marks every media as organic (Media.DED) and blocks the feed ad-injection " +
        "scheduler (BarcelonaSpoolFeedCacheHandler). Client-side only.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_THREADS)

    execute {
        // 1. Canonical isAd predicate -> always organic.
        MediaIsAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // 2. Ad-injection scheduler -> never inject (no ad slot created).
        InjectAdToFeedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
