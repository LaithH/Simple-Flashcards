package com.randomappsinc.simpleflashcards.home.dialogs;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class SortFlashcardSetsDialog implements ThemeManager.Listener {

    public interface Listener {
    }

    protected Listener listener;
    private MaterialDialog dialog;
    protected Context context;
    private ThemeManager themeManager = ThemeManager.get();

    public SortFlashcardSetsDialog(Context context, Listener listener) {
        this.listener = listener;
        this.context = context;
        createDialog(themeManager.getDarkModeEnabled(context));
        themeManager.registerListener(this);
    }

    public void createDialog(boolean darkModeEnabled) {
        int darkModeBackground = ContextCompat.getColor(context, R.color.dialog_dark_background);
        int white = ContextCompat.getColor(context, R.color.white);
        dialog = new MaterialDialog.Builder(context)
                .theme(darkModeEnabled ? Theme.DARK : Theme.LIGHT)
                .backgroundColor(darkModeEnabled ? darkModeBackground : white)
                .title(R.string.sort_flashcard_sets_title)
                .customView(R.layout.sort_flashcard_sets, true)
                .negativeText(R.string.cancel)
                .build();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        createDialog(darkModeEnabled);
    }

    public void cleanUp() {
        context = null;
        themeManager.unregisterListener(this);
    }
}
