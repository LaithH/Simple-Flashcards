package com.randomappsinc.simpleflashcards.editflashcards.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

public class FlashcardImageOptionsDialog implements ThemeManager.Listener {

    public interface Listener {
        void onFullViewRequested();

        void onFlashcardImageChangeRequested();

        void onFlashcardImageDeleted();
    }

    protected MaterialDialog optionsDialog;
    protected MaterialDialog confirmDeletionDialog;
    private Context context;
    protected Listener listener;
    private ThemeManager themeManager = ThemeManager.get();
    protected FlashcardDO flashcard;

    public FlashcardImageOptionsDialog(Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
        createDialogs();
        themeManager.registerListener(this);
    }

    private void createDialogs() {
        String[] options = context.getResources().getStringArray(R.array.flashcard_image_options);
        optionsDialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .items(options)
                .itemsCallback((dialog, itemView, position, text) -> {
                    switch (position) {
                        case 0:
                            listener.onFullViewRequested();
                            break;
                        case 1:
                            listener.onFlashcardImageChangeRequested();
                            break;
                        case 2:
                            confirmDeletionDialog.show();
                            break;
                        default:
                            throw new IllegalStateException("Unsupported index clicked for image options");
                    }
                })
                .build();
        confirmDeletionDialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.delete_image_confirmation_title)
                .content(R.string.delete_image_confirmation_body)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((dialog, which) -> listener.onFlashcardImageDeleted())
                .build();
    }

    public void show() {
        optionsDialog.show();
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        createDialogs();
    }

    public void cleanUp() {
        context = null;
        listener = null;
        themeManager.unregisterListener(this);
    }
}
