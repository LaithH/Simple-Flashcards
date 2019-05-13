package com.randomappsinc.simpleflashcards.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.backupandrestore.activities.BackupAndRestoreActivity;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class DialogUtil {

    public static void showHomepageDialog(final Activity activity) {
        PreferencesManager preferencesManager = new PreferencesManager(activity);
        final ThemeManager themeManager = ThemeManager.get();
        if (preferencesManager.isFirstTimeUser()) {
            preferencesManager.rememberWelcome();
            new MaterialDialog.Builder(activity)
                    .theme(themeManager.getDarkModeEnabled(activity) ? Theme.DARK : Theme.LIGHT)
                    .title(R.string.welcome)
                    .content(R.string.ask_for_help)
                    .positiveText(R.string.got_it)
                    .show();
        } else if (!preferencesManager.hasSeenBackupDataDialog()) {
            preferencesManager.rememberBackupDataDialogSeen();
            new MaterialDialog.Builder(activity)
                    .theme(themeManager.getDarkModeEnabled(activity) ? Theme.DARK : Theme.LIGHT)
                    .title(R.string.backup_your_data)
                    .content(R.string.backup_your_data_explanation)
                    .negativeText(R.string.backup_deny)
                    .positiveText(R.string.backup_confirm)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            activity.startActivity(new Intent(activity, BackupAndRestoreActivity.class));
                        }
                    })
                    .show();
        } else if (preferencesManager.shouldTeachAboutDarkMode()) {
            preferencesManager.rememberDarkModeDialogSeen();
            new MaterialDialog.Builder(activity)
                    .theme(Theme.LIGHT)
                    .title(R.string.dark_mode_tutorial_title)
                    .content(R.string.dark_mode_explanation)
                    .positiveText(R.string.sure_lets_do_it)
                    .negativeText(R.string.maybe_later)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            themeManager.setDarkModeEnabled(activity, true);
                        }
                    })
                    .cancelable(false)
                    .show();
        } else if (preferencesManager.shouldAskForRating()) {
            preferencesManager.rememberRatingDialogSeen();
            new MaterialDialog.Builder(activity)
                    .theme(themeManager.getDarkModeEnabled(activity) ? Theme.DARK : Theme.LIGHT)
                    .content(R.string.please_rate)
                    .negativeText(R.string.no_im_good)
                    .positiveText(R.string.sure_will_help)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            if (!(activity
                                    .getPackageManager()
                                    .queryIntentActivities(intent, 0).size() > 0)) {
                                UIUtils.showLongToast(R.string.play_store_error, activity);
                                return;
                            }
                            activity.startActivity(intent);
                        }
                    })
                    .show();
        } else if (preferencesManager.shouldAskForShare()) {
            preferencesManager.rememberSharingDialogSeen();
            new MaterialDialog.Builder(activity)
                    .theme(themeManager.getDarkModeEnabled(activity) ? Theme.DARK : Theme.LIGHT)
                    .title(R.string.studying_best_done_in_groups)
                    .content(R.string.please_share)
                    .negativeText(R.string.no_im_good)
                    .positiveText(R.string.sure_will_help)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
                                    .setType("text/plain")
                                    .setText(activity.getString(R.string.share_app_message))
                                    .getIntent();
                            if (shareIntent.resolveActivity(activity.getPackageManager()) != null) {
                                activity.startActivity(shareIntent);
                            }
                        }
                    })
                    .show();
        }
    }
}
