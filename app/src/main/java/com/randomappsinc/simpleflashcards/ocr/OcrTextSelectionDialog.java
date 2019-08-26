package com.randomappsinc.simpleflashcards.ocr;


import android.content.Context;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

import java.util.List;

/** Dialog to let user select text blocks after taking photo for OCR */
public class OcrTextSelectionDialog implements OcrTextSelectionAdapter.Listener, ThemeManager.Listener {

    public interface Listener {
        void onTextBlocksSelected(String flashcardText);
    }

    private MaterialDialog adderDialog;
    protected OcrTextSelectionAdapter textSelectionAdapter;
    protected Listener listener;
    private Context context;
    private ThemeManager themeManager = ThemeManager.get();

    public OcrTextSelectionDialog(Context context, Listener listenerImpl) {
        this.listener = listenerImpl;
        this.context = context;
        createDialog();
        themeManager.registerListener(this);
    }

    private void createDialog() {
        textSelectionAdapter = new OcrTextSelectionAdapter(this);
        adderDialog = new MaterialDialog.Builder(context)
                .theme(themeManager.getDarkModeEnabled(context) ? Theme.DARK : Theme.LIGHT)
                .title(R.string.select_text_from_text_blocks)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .adapter(textSelectionAdapter, null)
                .onPositive((dialog, which) -> listener.onTextBlocksSelected(textSelectionAdapter.getSelectedText()))
                .build();
        adderDialog.getRecyclerView().addItemDecoration(new SimpleDividerItemDecoration(context));
    }

    @Override
    public void onNumSelectedTextBlocksUpdated(int numSelectedSets) {
        adderDialog.getActionButton(DialogAction.POSITIVE).setEnabled(numSelectedSets > 0);
    }

    public void setTextBlocks(List<String> textBlocks) {
        textSelectionAdapter.setTextBlocks(textBlocks);
    }

    public void show() {
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