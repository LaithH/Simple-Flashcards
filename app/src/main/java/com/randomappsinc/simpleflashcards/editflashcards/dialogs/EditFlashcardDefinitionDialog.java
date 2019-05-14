package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class EditFlashcardDefinitionDialog implements ThemeManager.Listener {

    public interface Listener {
        void onFlashcardDefinitionEdited(String newDefinition);
    }

    private MaterialDialog dialog;
    private Context context;
    protected Listener listener;
    private ThemeManager themeManager = ThemeManager.get();

    public EditFlashcardDefinitionDialog(Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        dialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.flashcard_edit_definition_title)
                .alwaysCallInputCallback()
                .input(context.getString(R.string.definition),
                        "",
                        (dialog, input) -> {
                            String definition = input.toString();
                            boolean notEmpty = !definition.trim().isEmpty();
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(notEmpty);
                        })
                .positiveText(R.string.save)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    String newDefinition = dialog.getInputEditText().getText().toString().trim();
                    listener.onFlashcardDefinitionEdited(newDefinition);
                })
                .build();
        dialog.getInputEditText().setSingleLine(false);
    }

    public void show(String definition) {
        dialog.getInputEditText().setText(definition);
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
