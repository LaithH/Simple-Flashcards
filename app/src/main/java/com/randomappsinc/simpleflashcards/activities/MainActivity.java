package com.randomappsinc.simpleflashcards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.adapters.FlashcardSetsAdapter;
import com.randomappsinc.simpleflashcards.constants.Constants;
import com.randomappsinc.simpleflashcards.dialogs.DeleteFlashcardSetDialog;
import com.randomappsinc.simpleflashcards.dialogs.FlashcardSetCreatorDialog;
import com.randomappsinc.simpleflashcards.managers.BackupDataManager;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSet;
import com.randomappsinc.simpleflashcards.utils.UIUtils;
import com.randomappsinc.simpleflashcards.views.SimpleDividerItemDecoration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends StandardActivity
        implements FlashcardSetsAdapter.Listener, DeleteFlashcardSetDialog.Listener {

    @BindView(R.id.parent) View parent;
    @BindView(R.id.search_bar) View searchBar;
    @BindView(R.id.flashcard_set_search) EditText setSearch;
    @BindView(R.id.clear_search) View clearSearch;
    @BindView(R.id.flashcard_sets) RecyclerView sets;
    @BindView(R.id.no_sets) View noSetsAtAll;
    @BindView(R.id.no_sets_match) View noSetsMatch;
    @BindView(R.id.add_flashcard_set) FloatingActionButton addFlashcardSet;

    protected FlashcardSetsAdapter adapter;
    private FlashcardSetCreatorDialog flashcardSetCreatorDialog;
    private DeleteFlashcardSetDialog deleteFlashcardSetDialog;
    protected BackupDataManager backupDataManager = BackupDataManager.get();
    private DatabaseManager databaseManager = DatabaseManager.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PreferencesManager preferencesManager = new PreferencesManager(this);
        preferencesManager.logAppOpen();
        if (preferencesManager.isFirstTimeUser()) {
            preferencesManager.rememberWelcome();
            new MaterialDialog.Builder(this)
                    .title(R.string.welcome)
                    .content(R.string.ask_for_help)
                    .positiveText(android.R.string.yes)
                    .show();
        } else if (preferencesManager.shouldAskForRating()) {
            UIUtils.askForRating(this);
        } else if (preferencesManager.shouldAskForShare()) {
            UIUtils.askToShare(this);
        }

        addFlashcardSet.setImageDrawable(new IconDrawable(this, IoniconsIcons.ion_android_add)
                .colorRes(R.color.white)
                .actionBarSize());

        flashcardSetCreatorDialog = new FlashcardSetCreatorDialog(this, setCreatedListener);
        deleteFlashcardSetDialog = new DeleteFlashcardSetDialog(this, this);

        adapter = new FlashcardSetsAdapter(this, this);
        sets.addItemDecoration(new SimpleDividerItemDecoration(this));
        sets.setAdapter(adapter);

        databaseManager.setListener(databaseListener);
    }

    private final DatabaseManager.Listener databaseListener = new DatabaseManager.Listener() {
        @Override
        public void onDatabaseUpdated() {
            backupDataManager.backupData(getApplicationContext());
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        adapter.refreshContent(setSearch.getText().toString());
    }

    @OnTextChanged(value = R.id.flashcard_set_search, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable input) {
        adapter.refreshContent(input.toString());
        clearSearch.setVisibility(input.length() == 0 ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.clear_search)
    public void clearSearch() {
        setSearch.setText("");
    }

    @OnClick(R.id.add_flashcard_set)
    public void addSet() {
        flashcardSetCreatorDialog.show();
    }

    @OnClick(R.id.download_sets_button)
    public void downloadFlashcards() {
        startActivity(new Intent(this, QuizletSearchActivity.class));
    }

    @OnClick(R.id.create_set_button)
    public void createSet() {
        flashcardSetCreatorDialog.show();
    }

    @OnClick(R.id.share_with_nearby_button)
    public void shareWithNearby() {
        startActivity(new Intent(this, NearbySharingActivity.class));
    }

    private final FlashcardSetCreatorDialog.Listener setCreatedListener =
            new FlashcardSetCreatorDialog.Listener() {
                @Override
                public void onFlashcardSetCreated(int createdSetId) {
                    adapter.refreshContent(setSearch.getText().toString());
                    Intent intent = new Intent(MainActivity.this, EditFlashcardSetActivity.class);
                    intent.putExtra(Constants.FLASHCARD_SET_ID_KEY, createdSetId);
                    startActivity(intent);
                }
            };

    @Override
    public void browseFlashcardSet(FlashcardSet flashcardSet) {
        if (flashcardSet.getFlashcards().isEmpty()) {
            UIUtils.showSnackbar(
                    parent,
                    getString(R.string.no_flashcards_for_browsing),
                    Snackbar.LENGTH_LONG);
        } else {
            startActivity(new Intent(
                    this, BrowseFlashcardsActivity.class)
                    .putExtra(Constants.FLASHCARD_SET_ID_KEY, flashcardSet.getId()));
        }
    }

    @Override
    public void takeQuiz(FlashcardSet flashcardSet) {
        if (flashcardSet.getFlashcards().size() < 2) {
            UIUtils.showSnackbar(
                    parent,
                    getString(R.string.not_enough_for_quiz),
                    Snackbar.LENGTH_LONG);
        } else {
            startActivity(new Intent(
                    this, QuizSettingsActivity.class)
                    .putExtra(Constants.FLASHCARD_SET_ID_KEY, flashcardSet.getId()));
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
        }
    }

    @Override
    public void editFlashcardSet(FlashcardSet flashcardSet) {
        startActivity(new Intent(
                this, EditFlashcardSetActivity.class)
                .putExtra(Constants.FLASHCARD_SET_ID_KEY, flashcardSet.getId()));
    }

    @Override
    public void deleteFlashcardSet(FlashcardSet flashcardSet) {
        deleteFlashcardSetDialog.show(flashcardSet.getId());
    }

    @Override
    public void onFlashcardSetDeleted() {
        adapter.onFlashcardSetDeleted();
    }

    @Override
    public void onContentUpdated(int numSets) {
        if (DatabaseManager.get().getNumFlashcardSets() == 0) {
            searchBar.setVisibility(View.GONE);
            sets.setVisibility(View.GONE);
            noSetsMatch.setVisibility(View.GONE);
            noSetsAtAll.setVisibility(View.VISIBLE);
        } else {
            noSetsAtAll.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            if (numSets == 0) {
                sets.setVisibility(View.GONE);
                noSetsMatch.setVisibility(View.VISIBLE);
            } else {
                noSetsMatch.setVisibility(View.GONE);
                sets.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseManager.setListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        UIUtils.loadMenuIcon(menu, R.id.download_flashcard_sets, IoniconsIcons.ion_android_download, this);
        UIUtils.loadMenuIcon(menu, R.id.share_with_nearby, IoniconsIcons.ion_arrow_swap, this);
        UIUtils.loadMenuIcon(menu, R.id.settings, IoniconsIcons.ion_android_settings, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_flashcard_sets:
                startActivity(new Intent(this, QuizletSearchActivity.class));
                return true;
            case R.id.share_with_nearby:
                startActivity(new Intent(this, NearbySharingActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
