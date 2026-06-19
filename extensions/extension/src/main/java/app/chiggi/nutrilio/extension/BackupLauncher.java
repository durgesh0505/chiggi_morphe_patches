package app.chiggi.nutrilio.extension;

import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Surfaces Nutrilio's built-in — but UI-hidden — local file backup/restore screen
 * ({@code net.nutrilio.view.activities.DebugBackupActivity}) by injecting a button into the
 * Google-Drive {@code BackupActivity}.
 *
 * <p>{@code DebugBackupActivity} already exports a complete backup to a {@code .nutrilio} file
 * (a ZIP containing all DB entries, image assets, and every setting via the app's BackupPrefKey
 * converters) through a share sheet, and restores one via the Storage Access Framework — all free,
 * no premium gate. It is {@code exported=true} and its {@code onCreate} guards a null intent Uri, so
 * launching it with an empty Intent opens straight to its Export / Import buttons.
 *
 * <p>The button is added to {@code android.R.id.content} after layout (posted to the decor view), so
 * the injection point in {@code BackupActivity.onCreate} does not depend on where setContentView
 * runs. Only framework types are used; the target activity is referenced by name so a renamed
 * package (Change package name patch) still resolves it via {@link Activity#getPackageName()}.
 */
public final class BackupLauncher {

    private static final String DEBUG_BACKUP_ACTIVITY =
            "net.nutrilio.view.activities.DebugBackupActivity";
    private static final String BUTTON_TAG = "morphe_local_backup_btn";

    public static void install(final Activity activity) {
        if (activity == null) return;
        try {
            activity.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        addButton(activity);
                    } catch (Throwable ignored) {
                        // Never let UI injection crash the host screen.
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static void addButton(final Activity activity) {
        View contentView = activity.findViewById(android.R.id.content);
        if (!(contentView instanceof ViewGroup)) return;
        ViewGroup content = (ViewGroup) contentView;
        if (content.findViewWithTag(BUTTON_TAG) != null) return; // already added (e.g. config change)

        Button button = new Button(activity);
        button.setTag(BUTTON_TAG);
        button.setAllCaps(false);
        button.setText("Local backup & restore");
        int padV = dp(activity, 12);
        int padH = dp(activity, 20);
        button.setPadding(padH, padV, padH, padV);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = dp(activity, 24);
        content.addView(button, lp);

        button.setOnClickListener(new View.OnClickListener() {
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
    }

    private static int dp(Activity activity, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics());
    }

    private BackupLauncher() {
    }
}
