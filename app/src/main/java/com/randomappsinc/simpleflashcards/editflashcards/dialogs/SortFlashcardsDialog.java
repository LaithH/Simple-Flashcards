package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class SortFlashcardsDialog implements ThemeManager.Listener {

    public interface Listener {
        void onSortTermAscending();

        void onSortTermDescending();

        void onSortDefinitionAscending();

        void onSortDefinitionDescending();
    }

    protected Listener listener;
    private MaterialDialog dialog;
    protected Context context;
    private ThemeManager themeManager = ThemeManager.get();

    public SortFlashcardsDialog(Context context, Listener listener) {
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
                .title(R.string.sort_flashcards_title)
                .items(R.array.sort_flashcards_options)
                .negativeText(R.string.cancel)
                .itemsCallback((dialog, itemView, position, text) -> {
                    switch (position) {
                        case 0:
                            listener.onSortTermAscending();
                            break;
                        case 1:
                            listener.onSortTermDescending();
                            break;
                        case 2:
                            listener.onSortDefinitionAscending();
                            break;
                        case 3:
                            listener.onSortDefinitionDescending();
                            break;
                    }
                })
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
