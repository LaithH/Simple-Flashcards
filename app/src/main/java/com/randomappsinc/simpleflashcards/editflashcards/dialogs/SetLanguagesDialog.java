package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.constants.Language;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetLanguagesDialog implements ThemeManager.Listener {

    public interface Listener {
        void onLanguagesSelected(@Language int termsLanguage, @Language int definitionsLanguage);
    }

    @BindView(R.id.term_language_options) Spinner termOptions;
    @BindView(R.id.definition_language_options) Spinner definitionOptions;

    protected Listener listener;
    private MaterialDialog dialog;
    protected Context context;
    private ThemeManager themeManager = ThemeManager.get();
    protected int termIndex;
    protected int definitionIndex;

    public SetLanguagesDialog(Context context, FlashcardSetDO flashcardSet, Listener listener) {
        this.listener = listener;
        this.context = context;

        termIndex = getIndexFromLanguage(flashcardSet.getTermsLanguage());
        definitionIndex = getIndexFromLanguage(flashcardSet.getDefinitionsLanguage());

        createDialog(themeManager.getDarkModeEnabled(context));
        themeManager.registerListener(this);
    }

    public void createDialog(boolean darkModeEnabled) {
        int darkModeBackground = ContextCompat.getColor(context, R.color.dialog_dark_background);
        int white = ContextCompat.getColor(context, R.color.white);
        dialog = new MaterialDialog.Builder(context)
                .theme(darkModeEnabled ? Theme.DARK : Theme.LIGHT)
                .backgroundColor(darkModeEnabled ? darkModeBackground : white)
                .title(R.string.set_languages_title)
                .customView(R.layout.choose_set_languages, true)
                .positiveText(R.string.apply)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        termIndex = termOptions.getSelectedItemPosition();
                        definitionIndex = definitionOptions.getSelectedItemPosition();
                        listener.onLanguagesSelected(getLanguageFromIndex(termIndex), getLanguageFromIndex(definitionIndex));
                        UIUtils.showShortToast(R.string.language_settings_applied, context);
                    }
                })
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        termOptions.setSelection(termIndex);
                        definitionOptions.setSelection(definitionIndex);
                    }
                })
                .cancelable(false)
                .build();
        ButterKnife.bind(this, dialog.getCustomView());

        int textColor = darkModeEnabled ? white : ContextCompat.getColor(context, R.color.dark_gray);
        String[] languageChoices = context.getResources().getStringArray(R.array.language_options);

        termOptions.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_item, languageChoices));
        termOptions.getBackground().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        termOptions.setSelection(termIndex);

        definitionOptions.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_item, languageChoices));
        definitionOptions.getBackground().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        definitionOptions.setSelection(definitionIndex);
    }

    private int getIndexFromLanguage(@Language int language) {
        switch (language) {
            case Language.ENGLISH:
                return 0;
            case Language.SPANISH:
                return 1;
            case Language.FRENCH:
                return 2;
            case Language.JAPANESE:
                return 3;
            case Language.PORTUGUESE:
                return 4;
            case Language.CHINESE:
                return 5;
            case Language.GERMAN:
                return 6;
            case Language.ITALIAN:
                return 7;
            case Language.KOREAN:
                return 8;
            case Language.HINDI:
                return 9;
            case Language.BENGALI:
                return 10;
            case Language.RUSSIAN:
                return 11;
            case Language.NORWEGIAN:
                return 12;
            default:
                throw new IllegalArgumentException("Unsupported language!");
        }
    }

    protected @Language int getLanguageFromIndex(int index) {
        switch (index) {
            case 0:
                return Language.ENGLISH;
            case 1:
                return Language.SPANISH;
            case 2:
                return Language.FRENCH;
            case 3:
                return Language.JAPANESE;
            case 4:
                return Language.PORTUGUESE;
            case 5:
                return Language.CHINESE;
            case 6:
                return Language.GERMAN;
            case 7:
                return Language.ITALIAN;
            case 8:
                return Language.KOREAN;
            case 9:
                return Language.HINDI;
            case 10:
                return Language.BENGALI;
            case 11:
                return Language.RUSSIAN;
            case 12:
                return Language.NORWEGIAN;
            default:
                throw new IllegalArgumentException("Unsupported index!");
        }
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
