package app.chiggi.googlephone.extension;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.View;

/**
 * Opens the Contacts screen when the injected bottom-navigation "Contacts" button is tapped.
 *
 * <p>Google Phone v235 removed the in-app Contacts tab (only {@code MainFragmentPeer} and the tab
 * enum still name {@code CHOCOLATE_CHIP_KEY}; no tab fragment provider binds it, so selecting it as a
 * real tab throws {@code NoSuchElementException}). This mirrors the app's own hamburger-drawer
 * "Contacts" entry ({@code ContactsScreenProviderImpl}) instead: launch Google Contacts' in-dialer
 * leaf page if present, otherwise the default contacts viewer.
 *
 * <p>Called from the bottom-nav item-selected handler with the NavigationBarView as the context
 * source. Everything is wrapped so a resolve/launch failure can never crash the host screen.
 */
public final class BottomNavContacts {

    private BottomNavContacts() {}

    /** @param anchor the tapped NavigationBarView (used only for its Context). */
    public static void open(View anchor) {
        if (anchor == null) return;
        Context ctx = anchor.getContext();
        if (ctx == null) return;

        // Preferred: Google Contacts' in-dialer "leaf" page (same intent the drawer Contacts uses).
        try {
            Intent leaf = new Intent("com.google.android.apps.contacts.action.DIALER_LEAF");
            if (leaf.resolveActivity(ctx.getPackageManager()) != null) {
                ctx.startActivity(leaf);
                return;
            }
        } catch (Throwable ignored) {
            // fall through to the default contacts viewer
        }

        // Fallback: whatever app handles VIEW on the contacts collection.
        try {
            Intent view = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
            ctx.startActivity(view);
        } catch (Throwable ignored) {
            // nothing else to try; never crash the caller
        }
    }
}
