package com.randomappsinc.simpleflashcards.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.adapters.FlashcardSetSelectionAdapter;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSet;
import com.randomappsinc.simpleflashcards.views.SimpleDividerItemDecoration;

import java.util.List;

/** Dialog to let user choose flashcard sets to put into a folder */
public class FlashcardSetSelectionDialog implements FlashcardSetSelectionAdapter.Listener {

    public interface Listener {
        void onFlashcardSetsSelected(List<FlashcardSet> flashcardSets);
    }

    private MaterialDialog adderDialog;
    protected Listener listener;
    protected FlashcardSetSelectionAdapter setsAdapter;

    public FlashcardSetSelectionDialog(Context context, Listener listenerImpl) {
        this.listener = listenerImpl;
        setsAdapter = new FlashcardSetSelectionAdapter(this);
        adderDialog = new MaterialDialog.Builder(context)
                .title(R.string.add_flashcard_sets)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .adapter(setsAdapter, null)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onFlashcardSetsSelected(setsAdapter.getSelectedSets());
                    }
                })
                .build();
        adderDialog.getRecyclerView().addItemDecoration(new SimpleDividerItemDecoration(context));
    }

    @Override
    public void onNumSelectedSetsUpdated(int numSelectedSets) {
        adderDialog.getActionButton(DialogAction.POSITIVE).setEnabled(numSelectedSets > 0);
    }

    public void setFlashcardSetList(List<FlashcardSet> flashcardSets) {
        setsAdapter.setFlashcardSets(flashcardSets);
    }

    public void show() {
        adderDialog.show();
    }
}
