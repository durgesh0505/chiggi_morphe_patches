package app.chiggi.nutrilio.patches.search

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * il.r0 is the day/meal form's RecyclerView adapter. It overrides RecyclerView.Adapter, so R8 keeps
 * the framework method name onAttachedToRecyclerView. Hooking it gives the form's RecyclerView the
 * moment the adapter attaches, which is the single entry point used to install the food search.
 *
 * Class name (il.r0) is R8-obfuscated and pinned to net.nutrilio 1.20.2; re-verify on app updates.
 */
internal object FormAdapterAttachedFingerprint : Fingerprint(
    name = "onAttachedToRecyclerView",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroidx/recyclerview/widget/RecyclerView;"),
    definingClass = "Lil/r0;",
)
