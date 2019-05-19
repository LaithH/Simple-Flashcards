package com.randomappsinc.simpleflashcards.editflashcards.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.editflashcards.adapters.FlashcardOrderingAdapter;
import com.randomappsinc.simpleflashcards.editflashcards.adapters.SimpleItemTouchHelperCallback;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FlashcardOrderingActivity extends StandardActivity {

    @BindView(R.id.flashcards_list) RecyclerView flashcardsList;

    private FlashcardOrderingAdapter flashcardOrderingAdapter;
    private DatabaseManager databaseManager = DatabaseManager.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashcard_ordering);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int setId = getIntent().getIntExtra(Constants.FLASHCARD_SET_ID_KEY, 0);
        setTitle(databaseManager.getFlashcardSet(setId).getName());
        List<FlashcardDO> flashcardList = databaseManager.getAllFlashcards(setId);
        flashcardOrderingAdapter = new FlashcardOrderingAdapter(flashcardList);
        flashcardsList.setAdapter(flashcardOrderingAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(flashcardOrderingAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(flashcardsList);

        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (preferencesManager.shouldTeachFlashcardsReorder()) {
            new MaterialDialog.Builder(this)
                    .theme(themeManager.getDarkModeEnabled(this) ? Theme.DARK : Theme.LIGHT)
                    .title(R.string.reordering_flashcards)
                    .content(R.string.reorder_flashcards_instructions)
                    .positiveText(R.string.got_it)
                    .cancelable(false)
                    .show();
        }
    }

    @OnClick(R.id.save)
    public void saveOrder() {
        databaseManager.setFlashcardPositions(flashcardOrderingAdapter.getFlashcards());
        UIUtils.showShortToast(R.string.flashcards_reordered, this);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reorder_flashcards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_term_ascending:
                flashcardOrderingAdapter.sortFlashcards(
                        (first, second) -> first.getTerm().toLowerCase()
                                .compareTo(second.getTerm().toLowerCase()));
                flashcardsList.scrollToPosition(0);
                return true;
            case R.id.sort_by_term_descending:
                flashcardOrderingAdapter.sortFlashcards(
                        (first, second) -> first.getTerm().toLowerCase()
                                .compareTo(second.getTerm().toLowerCase()) * -1);
                flashcardsList.scrollToPosition(0);
                return true;
            case R.id.sort_by_definition_ascending:
                flashcardOrderingAdapter.sortFlashcards(
                        (first, second) -> first.getDefinition().toLowerCase()
                                .compareTo(second.getDefinition().toLowerCase()));
                flashcardsList.scrollToPosition(0);
                return true;
            case R.id.sort_by_definition_descending:
                flashcardOrderingAdapter.sortFlashcards(
                        (first, second) -> first.getDefinition().toLowerCase()
                                .compareTo(second.getDefinition().toLowerCase()) * -1);
                flashcardsList.scrollToPosition(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
