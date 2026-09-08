package app.chiggi.googlephone.patches.bottomnav

import app.chiggi.googlephone.patches.shared.Constants.COMPATIBILITY_GOOGLE_PHONE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val addContactsBottomTabPatch = bytecodePatch(
    name = "Contacts on bottom bar",
    description = "Adds a Contacts button to the bottom navigation bar, between Home and Keypad. " +
        "Tapping it opens the Contacts app. NOTE: Google removed the in-app Contacts tab from this " +
        "build (the tab fragment is gone — selecting it as a real tab throws), so this routes the " +
        "button to the Contacts app, matching the app's own drawer Contacts entry. Version-locked to " +
        "235.x: the handler hook references the obfuscated listener class, so it needs re-fingerprinting " +
        "after a Google Phone update.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GOOGLE_PHONE)

    // Adds the Contacts menu item to the bottom-nav menu, and pulls in the BottomNavContacts extension.
    dependsOn(addContactsMenuItemPatch)
    extendWith("extensions/extension.mpe")

    execute {
        // MainFragmentPeer's bottom-nav item-selected listener (Lnfo;.a(MenuItem)V): when the tapped
        // item is R.id.tab_contacts (0x7f0b05cf), open the Contacts app via the extension and return,
        // before the listener maps it to a tab (which would throw — no contacts fragment provider).
        // Lnfo;->a:Lxmn; is the captured NavigationBarView (a View), used only as the Context source.
        // p0 = this (Lnfo;), p1 = MenuItem. The method has >=10 registers, so v0/v1 are free scratch.
        BottomNavItemSelectedFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-interface { p1 }, Landroid/view/MenuItem;->getItemId()I
                move-result v0
                const v1, 0x7f0b05cf
                if-ne v0, v1, :original
                iget-object v0, p0, Lnfo;->a:Lxmn;
                invoke-static { v0 }, Lapp/chiggi/googlephone/extension/BottomNavContacts;->open(Landroid/view/View;)V
                return-void
                :original
                nop
            """,
        )
    }
}
