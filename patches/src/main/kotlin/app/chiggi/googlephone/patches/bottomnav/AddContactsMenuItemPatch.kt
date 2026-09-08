package app.chiggi.googlephone.patches.bottomnav

import app.chiggi.googlephone.patches.shared.Constants.COMPATIBILITY_GOOGLE_PHONE
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Adds the Contacts item to the current bottom-nav menu (`res/menu/homepage_bottom_nav_menu.xml`,
 * used by `drawer_main_fragment.xml`), inserted between Home (`tab_call_history`) and Keypad
 * (`tab_dialpad`) so the row reads Home · Contacts · Keypad. Reuses the app's existing `tab_contacts`
 * id, `bottom_nav_ic_contacts_vd_theme_24` drawable and `tab_title_contacts` string (all still present
 * from the legacy `navigation_bar_main_menu`). Dependency of `addContactsBottomTabPatch`, which wires
 * the tap; do not enable alone (a Contacts item with no handler throws in the selection listener).
 */
@Suppress("unused")
val addContactsMenuItemPatch = resourcePatch(
    name = "Contacts on bottom bar (menu item)",
    description = "Adds the Contacts entry to the bottom navigation menu, between Home and Keypad. " +
        "Pulled in automatically by \"Contacts on bottom bar\"; not useful on its own.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GOOGLE_PHONE)

    execute {
        document("res/menu/homepage_bottom_nav_menu.xml").use { document ->
            val menu = document.getElementsByTagName("menu").item(0) as Element

            // Skip if a Contacts item is somehow already present (idempotent).
            val items = document.getElementsByTagName("item")
            var dialpad: Element? = null
            for (i in 0 until items.length) {
                val el = items.item(i) as Element
                val id = el.getAttribute("android:id")
                if (id.endsWith("tab_contacts")) return@use
                if (id.endsWith("tab_dialpad")) dialpad = el
            }

            val contacts = document.createElement("item").apply {
                setAttribute("android:id", "@id/tab_contacts")
                setAttribute("android:icon", "@drawable/bottom_nav_ic_contacts_vd_theme_24")
                setAttribute("android:title", "@string/tab_title_contacts")
            }

            // Home · [Contacts] · Keypad. If the dialpad item isn't found by name, append.
            if (dialpad != null) menu.insertBefore(contacts, dialpad) else menu.appendChild(contacts)
        }
    }
}
