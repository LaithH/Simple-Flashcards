package com.randomappsinc.simpleflashcards.editflashcards.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.dialogs.ConfirmQuitDialog;
import com.randomappsinc.simpleflashcards.editflashcards.adapters.FlashcardOrderingAdapter;
import com.randomappsinc.simpleflashcards.editflashcards.adapters.SimpleItemTouchHelperCallback;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.SortFlashcardsDialog;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReorderFlashcardsActivity extends StandardActivity
        implements ConfirmQuitDialog.Listener, SortFlashcardsDialog.Listener {

    @BindView(R.id.flashcards_list) RecyclerView flashcardsList;

    private FlashcardOrderingAdapter flashcardOrderingAdapter;
    private DatabaseManager databaseManager = DatabaseManager.get();
    private ConfirmQuitDialog confirmQuitDialog;
    private @Nullable SortFlashcardsDialog sortFlashcardsDialog;

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

        confirmQuitDialog = new ConfirmQuitDialog(
                this, this, R.string.confirm_reorder_exit_body);
    }

    @OnClick(R.id.save)
    public void saveOrder() {
        databaseManager.setFlashcardPositions(flashcardOrderingAdapter.getFlashcards());
        UIUtils.showShortToast(R.string.flashcards_reordered, this);
        finish();
    }

    @Override
    public void onQuitConfirmed() {
        finish();
    }

    @Override
    public void onSortTermAscending() {
        flashcardOrderingAdapter.sortFlashcards(
                (first, second) -> first.getTerm().toLowerCase()
                        .compareTo(second.getTerm().toLowerCase()));
        flashcardsList.scrollToPosition(0);
    }

    @Override
    public void onSortTermDescending() {
        flashcardOrderingAdapter.sortFlashcards(
                (first, second) -> first.getTerm().toLowerCase()
                        .compareTo(second.getTerm().toLowerCase()) * -1);
        flashcardsList.scrollToPosition(0);
    }

    @Override
    public void onSortDefinitionAscending() {
        flashcardOrderingAdapter.sortFlashcards(
                (first, second) -> first.getDefinition().toLowerCase()
                        .compareTo(second.getDefinition().toLowerCase()));
        flashcardsList.scrollToPosition(0);
    }

    @Override
    public void onSortDefinitionDescending() {
        flashcardOrderingAdapter.sortFlashcards(
                (first, second) -> first.getDefinition().toLowerCase()
                        .compareTo(second.getDefinition().toLowerCase()) * -1);
        flashcardsList.scrollToPosition(0);
    }

    @Override
    public void onBackPressed() {
        confirmQuitDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        confirmQuitDialog.cleanUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reorder_flashcards, menu);
        UIUtils.loadMenuIcon(menu, R.id.sort, FontAwesomeIcons.fa_sort_alpha_asc, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sort) {
            if (sortFlashcardsDialog == null) {
                sortFlashcardsDialog = new SortFlashcardsDialog(this, this);
            }
            sortFlashcardsDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
