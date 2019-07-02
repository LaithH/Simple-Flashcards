package com.randomappsinc.simpleflashcards.home.dialogs;

import android.content.Context;
import android.widget.RadioButton;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortFlashcardSetsDialog implements ThemeManager.Listener {

    public interface Listener {
        void onSortAlphabeticalAscending();

        void onSortAlphabeticalDescending();

        void onLeastLearnedFirst();

        void onMostLearnedFirst();
    }

    @BindView(R.id.alphabetical_ascending) RadioButton alphabeticalAscending;
    @BindView(R.id.alphabetical_descending) RadioButton alphabeticalDescending;
    @BindView(R.id.least_learned_first) RadioButton leastLearnedFirst;
    @BindView(R.id.most_learned_first) RadioButton mostLearnedFirst;

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
                .positiveText(R.string.apply)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (alphabeticalAscending.isChecked()) {
                        listener.onSortAlphabeticalAscending();
                    } else if (alphabeticalDescending.isChecked()) {
                        listener.onSortAlphabeticalDescending();
                    } else if (leastLearnedFirst.isChecked()) {
                        listener.onLeastLearnedFirst();
                    } else if (mostLearnedFirst.isChecked()) {
                        listener.onMostLearnedFirst();
                    }
                })
                .build();
        ButterKnife.bind(this, dialog.getCustomView());
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
