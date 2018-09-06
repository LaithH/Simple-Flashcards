package com.randomappsinc.simpleflashcards.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.randomappsinc.simpleflashcards.R;

public class UIUtils {

    public static void closeKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showSnackbar(View parent, String message, int length) {
        Snackbar snackbar = Snackbar.make(parent, message, length);
        View rootView = snackbar.getView();
        snackbar.getView().setBackgroundColor(parent
                .getContext()
                .getResources()
                .getColor(R.color.app_blue));
        TextView tv = rootView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public static void showShortToast(@StringRes int stringId, Context context) {
        showToast(stringId, Toast.LENGTH_SHORT, context);
    }

    public static void showLongToast(@StringRes int stringId, Context context) {
        showToast(stringId, Toast.LENGTH_LONG, context);
    }

    protected static void showToast(@StringRes int stringId, int duration, Context context) {
        Toast.makeText(context, stringId, duration).show();
    }

    public static void showLongToast(String text, Context context) {
        showToast(text, Toast.LENGTH_LONG, context);
    }

    private static void showToast(String text, int duration, Context context) {
        Toast.makeText(context, text, duration).show();
    }

    public static void askForRating(final Activity activity) {
        new MaterialDialog.Builder(activity)
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
                            UIUtils.showToast(R.string.play_store_error, Toast.LENGTH_LONG, activity);
                            return;
                        }
                        activity.startActivity(intent);
                    }
                })
                .show();
    }

    public static void askToShare(final Activity activity) {
        new MaterialDialog.Builder(activity)
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

    public static void loadMenuIcon(Menu menu, int itemId, Icon icon, Context context) {
        menu.findItem(itemId).setIcon(
                new IconDrawable(context, icon)
                        .colorRes(R.color.white)
                        .actionBarSize());
    }
}
