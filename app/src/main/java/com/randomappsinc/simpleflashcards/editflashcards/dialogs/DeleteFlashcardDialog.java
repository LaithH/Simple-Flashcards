package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class DeleteFlashcardDialog implements ThemeManager.Listener {

    public interface Listener {
        void onFlashcardDeleted();
    }

    private MaterialDialog dialog;
    private Context context;
    protected Listener listener;
    private ThemeManager themeManager = ThemeManager.get();

    public DeleteFlashcardDialog(Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        dialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.flashcard_delete_title)
                .content(R.string.flashcard_delete_message)
                .positiveText(R.string.yes)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> listener.onFlashcardDeleted())
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
