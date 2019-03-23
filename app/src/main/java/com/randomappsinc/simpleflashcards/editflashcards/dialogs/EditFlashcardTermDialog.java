package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.models.Flashcard;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

import androidx.annotation.NonNull;

public class EditFlashcardTermDialog implements ThemeManager.Listener {

    public interface Listener {
        void onFlashcardTermEdited(String newTerm);
    }

    private MaterialDialog dialog;
    private Context context;
    protected Listener listener;
    private ThemeManager themeManager = ThemeManager.get();

    public EditFlashcardTermDialog(Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        dialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.flashcard_edit_term_title)
                .alwaysCallInputCallback()
                .input(context.getString(R.string.term),
                        "",
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                String setName = input.toString();
                                boolean notEmpty = !setName.trim().isEmpty();
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(notEmpty);
                            }
                        })
                .positiveText(R.string.save)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String newTerm = dialog.getInputEditText().getText().toString().trim();
                        listener.onFlashcardTermEdited(newTerm);
                    }
                })
                .build();
        dialog.getInputEditText().setSingleLine(false);
    }

    public void show(Flashcard flashcard) {
        dialog.getInputEditText().setText(flashcard.getTerm());
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
