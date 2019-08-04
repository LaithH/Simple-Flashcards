package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CreateFlashcardDialog implements MaterialDialog.SingleButtonCallback, ThemeManager.Listener {

    public interface Listener {
        void onFlashcardCreated(String term, String definition);

        void onVoiceTermEntryRequested();

        void onVoiceDefinitionEntryRequested();
    }

    @BindView(R.id.term_input) EditText termInput;
    @BindView(R.id.voice_term_entry) View voiceTermEntry;
    @BindView(R.id.duplicate_term_warning) View duplicateTermWarning;
    @BindView(R.id.definition_input) EditText definitionInput;
    @BindView(R.id.duplicate_definition_warning) View duplicateDefinitionWarning;
    @BindView(R.id.voice_definition_entry) View voiceDefinitionEntry;

    protected MaterialDialog dialog;
    protected Listener listener;
    private Context context;
    private int setId;
    private ThemeManager themeManager = ThemeManager.get();
    private DatabaseManager databaseManager = DatabaseManager.get();

    public CreateFlashcardDialog(Context context, @NonNull Listener listener, int setId) {
        this.context = context;
        this.listener = listener;
        this.setId = setId;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        dialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.create_flashcard)
                .customView(R.layout.create_flashcard, true)
                .positiveText(R.string.save)
                .onPositive(this)
                .neutralText(R.string.cancel)
                .cancelable(false)
                .build();
        ButterKnife.bind(this, dialog.getCustomView());
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if (which == DialogAction.POSITIVE) {
            String term = termInput.getText().toString().trim();
            String definition = definitionInput.getText().toString().trim();
            listener.onFlashcardCreated(term, definition);
        }
    }

    @OnTextChanged(value = R.id.term_input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTermInputChanged(Editable input) {
        voiceTermEntry.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        String term = input.toString().trim();
        boolean enableWarning = !term.isEmpty() && databaseManager.doesTermExist(setId, term);
        duplicateTermWarning.setVisibility(enableWarning ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.voice_term_entry)
    public void onVoiceTermEntry() {
        listener.onVoiceTermEntryRequested();
    }

    public void onVoiceTermSpoken(String term) {
        termInput.requestFocus();
        termInput.setText(term);
        termInput.setSelection(term.length());
    }

    @OnTextChanged(value = R.id.definition_input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onDefinitionInputChanged(Editable input) {
        voiceDefinitionEntry.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        String definition = input.toString().trim();
        boolean enableWarning = !definition.isEmpty() && databaseManager.doesDefinitionExist(setId, definition);
        duplicateDefinitionWarning.setVisibility(enableWarning ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.voice_definition_entry)
    public void onVoiceDefinitionEntry() {
        listener.onVoiceDefinitionEntryRequested();
    }

    public void onVoiceDefinitionSpoken(String term) {
        definitionInput.requestFocus();
        definitionInput.setText(term);
        definitionInput.setSelection(term.length());
    }

    public void show() {
        termInput.setText("");
        definitionInput.setText("");
        dialog.show();
        if (termInput.requestFocus()) {
            termInput.post(() -> {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) dialog
                                .getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(termInput, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
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
