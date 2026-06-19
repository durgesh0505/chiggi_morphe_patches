package app.chiggi.nutrilio.extension;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Surfaces Nutrilio's built-in — but UI-hidden — local file backup/restore on the Backup screen,
 * matching the app theme.
 *
 * <p>{@link #install(Activity)} (hooked into {@code BackupActivity.onCreate}) inserts a single native
 * {@code net.nutrilio.view.custom_views.MenuItemView} row — "Local backup &amp; restore" — immediately
 * above the existing "Restore Backup" row ({@code R.id.item_restore_backup}). The row is configured as
 * a navigation item ({@code setCheckable(false)} hides the on/off switch that MenuItemView shows by
 * default) and opens {@code DebugBackupActivity}, which holds the real, free local Export and Import
 * buttons. Nutrilio's own backup machinery is used unmodified; only the entry point is added.
 *
 * <p>Everything is resolved by resource-id name and the named {@code MenuItemView} class, so it
 * survives obfuscation, and every call is wrapped so a layout change can never crash the host screen.
 */
public final class BackupLauncher {

    private static final String DEBUG_BACKUP_ACTIVITY =
            "net.nutrilio.view.activities.DebugBackupActivity";
    private static final String MENU_ITEM_VIEW = "net.nutrilio.view.custom_views.MenuItemView";
    private static final String ROW_TAG = "morphe_local_backup_row";

    /** BackupActivity.onCreate hook: add the themed row above "Restore Backup". */
    public static void install(final Activity activity) {
        if (activity == null) return;
        try {
            activity.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        addRow(activity);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static void addRow(Activity activity) throws Exception {
        Resources res = activity.getResources();
        String pkg = activity.getPackageName();
        int restoreId = res.getIdentifier("item_restore_backup", "id", pkg);
        if (restoreId == 0) return;

        View restoreRow = activity.findViewById(restoreId);
        if (restoreRow == null) return;

        ViewParent parent = restoreRow.getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup container = (ViewGroup) parent;
        if (container.findViewWithTag(ROW_TAG) != null) return; // already inserted

        int restoreIndex = container.indexOfChild(restoreRow);
        if (restoreIndex < 0) return;

        View row = buildRow(activity);
        if (row == null) return;

        // Insert directly above "Restore Backup".
        container.addView(row, restoreIndex, cloneLayoutParams(restoreRow));
    }

    private static View buildRow(final Activity activity) {
        try {
            Class<?> cls = Class.forName(MENU_ITEM_VIEW);
            Constructor<?> ctor = cls.getConstructor(Context.class);
            Object menuItem = ctor.newInstance(activity);
            if (!(menuItem instanceof View)) return null;

            // Navigation row, not a toggle: hide the default on/off switch.
            try {
                Method setCheckable = cls.getMethod("setCheckable", boolean.class);
                setCheckable.invoke(menuItem, false);
            } catch (Throwable ignored) {
            }

            Method setTitle = cls.getMethod("setTitle", String.class);
            setTitle.invoke(menuItem, "Local backup & restore");

            View row = (View) menuItem;
            row.setTag(ROW_TAG);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName(activity.getPackageName(), DEBUG_BACKUP_ACTIVITY);
                        activity.startActivity(intent);
                    } catch (Throwable t) {
                        Toast.makeText(activity, "Backup screen unavailable", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return row;
        } catch (Throwable t) {
            return null;
        }
    }

    private static ViewGroup.LayoutParams cloneLayoutParams(View sibling) {
        ViewGroup.LayoutParams src = sibling.getLayoutParams();
        int width = src != null ? src.width : ViewGroup.LayoutParams.MATCH_PARENT;
        int height = src != null ? src.height : ViewGroup.LayoutParams.WRAP_CONTENT;
        return new ViewGroup.LayoutParams(width, height);
    }

    private BackupLauncher() {
    }
}
