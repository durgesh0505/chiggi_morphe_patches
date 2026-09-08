package app.chiggi.googlephone.patches.bottomnav

import app.morphe.patcher.Fingerprint

/**
 * The bottom-navigation item-selected handler: the synthetic `xmm` listener installed by
 * MainFragmentPeer.setUpNavigationBarItemSelectedListener (obfuscated class `Lnfo;`, method
 * `a(Landroid/view/MenuItem;)V` in v235.0). It maps the tapped menu-item id to a tab (ndw) and throws
 * "No such item as %s" for unknown ids — so a Contacts item added to the menu must be intercepted here
 * before it reaches that throw / the (removed) contacts tab fragment.
 *
 * Anchored on the two log/format string literals unique to this method; the obfuscated class name is
 * not stable across builds. `Lnfo;` holds field `a:Lxmn;` = the tapped NavigationBarView (a View).
 */
internal object BottomNavItemSelectedFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/view/MenuItem;"),
    strings = listOf(
        "No such item as %s",
        "setUpNavigationBarItemSelectedListener\$<anonymous>",
    ),
)
