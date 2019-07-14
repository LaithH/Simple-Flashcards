package com.randomappsinc.simpleflashcards.home.dialogs;

import android.content.Context;
import android.text.InputType;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class CreateFlashcardSetDialog implements ThemeManager.Listener {

    public interface Listener {
        void onFlashcardSetCreated(String newSetName);
    }

    private MaterialDialog adderDialog;
    private Context context;
    protected Listener listener;
    private ThemeManager themeManager = ThemeManager.get();

    public CreateFlashcardSetDialog(Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        adderDialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.create_flashcard_set_title)
                .alwaysCallInputCallback()
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(context.getString(R.string.flashcard_set_name),
                        "",
                        (dialog, input) -> {
                            String setName = input.toString();
                            boolean notEmpty = !setName.trim().isEmpty();
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(notEmpty);
                        })
                .positiveText(R.string.create)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    String setName = dialog.getInputEditText().getText().toString().trim();
                    listener.onFlashcardSetCreated(setName);
                })
                .build();
    }

    public void show() {
        adderDialog.getInputEditText().setText("");
        adderDialog.show();
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
