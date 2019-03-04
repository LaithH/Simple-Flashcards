package com.randomappsinc.simpleflashcards.editflashcards.managers;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.editflashcards.constants.ImportFlashcardsMode;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.SingleFlashcardSetChooserDialog;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSet;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class ImportFlashcardsManager implements ThemeManager.Listener, SingleFlashcardSetChooserDialog.Listener {

    public interface Listener {
        void onSetChosen(FlashcardSet flashcardSet, @ImportFlashcardsMode int importMode);
    }

    private Context context;
    private Listener listener;
    private MaterialDialog optionsDialog;
    protected SingleFlashcardSetChooserDialog setChooserDialog;
    protected @ImportFlashcardsMode int importMode;
    private ThemeManager themeManager = ThemeManager.get();

    public ImportFlashcardsManager(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        setChooserDialog = new SingleFlashcardSetChooserDialog(context, this);
        createDialogs();
        themeManager.registerListener(this);
    }

    private void createDialogs() {
        boolean darkModeEnabled = themeManager.getDarkModeEnabled(context);
        optionsDialog = new MaterialDialog.Builder(context)
                .theme(darkModeEnabled ? Theme.DARK : Theme.LIGHT)
                .title(R.string.import_flashcards_options_title)
                .items(context.getResources().getStringArray(R.array.import_flashcards_options))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(
                            MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        switch (position) {
                            case 0:
                                importMode = ImportFlashcardsMode.MOVE;
                                break;
                            case 1:
                                importMode = ImportFlashcardsMode.COPY;
                                break;
                        }
                        setChooserDialog.show(importMode == ImportFlashcardsMode.MOVE
                                ? R.string.move_flashcards_description
                                : R.string.copy_flashcards_description);
                    }
                })
                .positiveText(R.string.cancel)
                .build();
        setChooserDialog.createDialog(darkModeEnabled);
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        createDialogs();
    }

    public void startImport() {
        optionsDialog.show();
    }

    @Override
    public void onFlashcardSetChosen(FlashcardSet flashcardSet) {
        listener.onSetChosen(flashcardSet, importMode);
    }

    public void cleanUp() {
        context = null;
        themeManager.unregisterListener(this);
    }
}
