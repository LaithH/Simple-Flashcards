package com.randomappsinc.simpleflashcards.common.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class ConfirmQuitDialog implements ThemeManager.Listener {

    public interface Listener {
        void onQuitConfirmed();
    }

    private MaterialDialog dialog;
    private Context context;
    private @StringRes int bodyId;
    protected Listener listener;
    private ThemeManager themeManager = ThemeManager.get();

    public ConfirmQuitDialog(Context context, @NonNull Listener listener, @StringRes int bodyId) {
        this.context = context;
        this.bodyId = bodyId;
        this.listener = listener;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        dialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.confirm_exit)
                .content(bodyId)
                .positiveText(R.string.yes)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> listener.onQuitConfirmed())
                .build();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        createDialog();
    }

    public void cleanUp() {
        context = null;
        listener = null;
        themeManager.unregisterListener(this);
    }
}
