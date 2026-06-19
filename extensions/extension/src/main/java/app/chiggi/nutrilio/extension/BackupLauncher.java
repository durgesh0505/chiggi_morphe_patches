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
 * Surfaces Nutrilio's built-in — but UI-hidden — local file backup/restore directly on the Backup
 * screen, matching the app theme.
 *
 * <p>{@link #install(Activity)} (hooked into {@code BackupActivity.onCreate}) inserts two native
 * {@code net.nutrilio.view.custom_views.MenuItemView} rows — "Export backup file" and "Import backup
 * file" — immediately above the existing "Restore Backup" row ({@code R.id.item_restore_backup}).
 * Each row launches the hidden {@code DebugBackupActivity} with an action extra.
 *
 * <p>{@link #autoAction(Activity)} (hooked into {@code DebugBackupActivity.onCreate}) reads that
 * extra and performs a {@link View#performClick()} on the app's own export ({@code item_export_file})
 * or import ({@code item_import_file}) button. That runs Nutrilio's own, unmodified backup machinery
 * (the export's async build + share-sheet observer, the import's file-picker + restore-confirm), so
 * the behaviour is exactly the app's — only the entry point is new. Everything is resolved by
 * resource-id name and by the named {@code MenuItemView} class, so it survives obfuscation; all calls
 * are wrapped so a layout change can never crash the host screen.
 */
public final class BackupLauncher {

    private static final String DEBUG_BACKUP_ACTIVITY =
            "net.nutrilio.view.activities.DebugBackupActivity";
    private static final String MENU_ITEM_VIEW = "net.nutrilio.view.custom_views.MenuItemView";
    private static final String EXTRA_ACTION = "morphe_backup_action";
    private static final String ACTION_EXPORT = "export";
    private static final String ACTION_IMPORT = "import";
    private static final String TAG_EXPORT = "morphe_export_row";
    private static final String TAG_IMPORT = "morphe_import_row";

    /** BackupActivity.onCreate hook: add the two themed rows above "Restore Backup". */
    public static void install(final Activity activity) {
        if (activity == null) return;
        try {
            activity.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        addRows(activity);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static void addRows(Activity activity) throws Exception {
        Resources res = activity.getResources();
        String pkg = activity.getPackageName();
        int restoreId = res.getIdentifier("item_restore_backup", "id", pkg);
        if (restoreId == 0) return;

        View restoreRow = activity.findViewById(restoreId);
        if (restoreRow == null) return;

        ViewParent parent = restoreRow.getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup container = (ViewGroup) parent;
        if (container.findViewWithTag(TAG_EXPORT) != null) return; // already inserted

        int restoreIndex = container.indexOfChild(restoreRow);
        if (restoreIndex < 0) return;

        View exportRow = buildRow(activity, "Export backup file", ACTION_EXPORT, TAG_EXPORT);
        View importRow = buildRow(activity, "Import backup file", ACTION_IMPORT, TAG_IMPORT);
        if (exportRow == null || importRow == null) return;

        // Insert so the final order is: Export, Import, Restore Backup.
        container.addView(importRow, restoreIndex, cloneLayoutParams(restoreRow));
        container.addView(exportRow, restoreIndex, cloneLayoutParams(restoreRow));
    }

    private static View buildRow(final Activity activity, String title, final String action, String tag) {
        try {
            Class<?> cls = Class.forName(MENU_ITEM_VIEW);
            Constructor<?> ctor = cls.getConstructor(Context.class);
            Object menuItem = ctor.newInstance(activity);
            if (!(menuItem instanceof View)) return null;

            Method setTitle = cls.getMethod("setTitle", String.class);
            setTitle.invoke(menuItem, title);

            View row = (View) menuItem;
            row.setTag(tag);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName(activity.getPackageName(), DEBUG_BACKUP_ACTIVITY);
                        intent.putExtra(EXTRA_ACTION, action);
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

    /** DebugBackupActivity.onCreate hook: auto-fire the requested export/import button. */
    public static void autoAction(final Activity activity) {
        if (activity == null) return;
        try {
            Intent intent = activity.getIntent();
            if (intent == null) return;
            final String action = intent.getStringExtra(EXTRA_ACTION);
            if (action == null) return;

            activity.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String idName = ACTION_EXPORT.equals(action) ? "item_export_file" : "item_import_file";
                        int id = activity.getResources().getIdentifier(idName, "id", activity.getPackageName());
                        if (id == 0) return;
                        View button = activity.findViewById(id);
                        if (button != null) button.performClick();
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private BackupLauncher() {
    }
}
