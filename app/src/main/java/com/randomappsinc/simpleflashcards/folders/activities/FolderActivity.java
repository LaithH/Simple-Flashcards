package com.randomappsinc.simpleflashcards.folders.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.folders.adapters.FolderSetsAdapter;
import com.randomappsinc.simpleflashcards.folders.dialogs.FlashcardSetSelectionDialog;
import com.randomappsinc.simpleflashcards.folders.dialogs.RenameFolderDialog;
import com.randomappsinc.simpleflashcards.folders.models.Folder;
import com.randomappsinc.simpleflashcards.home.activities.FlashcardSetActivity;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FolderActivity extends StandardActivity implements FlashcardSetSelectionDialog.Listener,
        FolderSetsAdapter.Listener, RenameFolderDialog.Listener {

    @BindView(R.id.add_sets) FloatingActionButton addSetsButton;
    @BindView(R.id.no_sets) View noSets;
    @BindView(R.id.flashcard_sets) RecyclerView setsList;

    private int folderId;
    private DatabaseManager databaseManager = DatabaseManager.get();
    private FlashcardSetSelectionDialog setAdderDialog;
    private FolderSetsAdapter setsAdapter;
    private RenameFolderDialog renameFolderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        addSetsButton.setImageDrawable(new IconDrawable(this, IoniconsIcons.ion_android_add)
                .colorRes(R.color.white)
                .actionBarSize());

        folderId = getIntent().getIntExtra(Constants.FOLDER_ID_KEY, 0);
        Folder folder = databaseManager.getFolder(folderId);
        setTitle(folder.getName());

        setsAdapter = new FolderSetsAdapter(this);
        setsList.setAdapter(setsAdapter);
        setsList.addItemDecoration(new SimpleDividerItemDecoration(this));

        setAdderDialog = new FlashcardSetSelectionDialog(this, this);
        setAdderDialog.setFlashcardSetList(databaseManager.getFlashcardSetsNotInFolder(folderId));

        renameFolderDialog = new RenameFolderDialog(this, folder.getName(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSetsList();
    }

    // Syncs the sets adapter against what's in DB
    private void updateSetsList() {
        setsAdapter.refreshContent(databaseManager.getFlashcardSetsInFolder(folderId));
        setsList.setVisibility(setsAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        noSets.setVisibility(setsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.add_sets)
    public void addFlashcardSets() {
        if (setAdderDialog.getNumSets() > 0) {
            setAdderDialog.show();
        } else {
            UIUtils.showLongToast(R.string.no_sets_to_add, this);
        }
    }

    @Override
    public void onFlashcardSetsSelected(List<FlashcardSetDO> flashcardSets) {
        databaseManager.addFlashcardSetsIntoFolder(folderId, flashcardSets);
        setAdderDialog.setFlashcardSetList(databaseManager.getFlashcardSetsNotInFolder(folderId));
        updateSetsList();
        UIUtils.showShortToast(flashcardSets.size() == 1
                ? R.string.flashcard_set_added
                : R.string.flashcard_sets_added, this);
    }

    @Override
    public void onFlashcardSetClicked(FlashcardSetDO flashcardSetDO) {
        Intent intent = new Intent(this, FlashcardSetActivity.class);
        intent.putExtra(Constants.FLASHCARD_SET_ID_KEY, flashcardSetDO.getId());
        startActivity(intent);
    }

    @Override
    public void removeFlashcardSet(FlashcardSetDO flashcardSet) {
        databaseManager.removeFlashcardSetFromFolder(folderId, flashcardSet);
        setsList.setVisibility(setsAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        noSets.setVisibility(setsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        setAdderDialog.setFlashcardSetList(databaseManager.getFlashcardSetsNotInFolder(folderId));
    }

    @Override
    public void onFolderName(String newFolderName) {
        databaseManager.renameFolder(folderId, newFolderName);
        setTitle(newFolderName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setAdderDialog.cleanUp();
        renameFolderDialog.cleanUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        UIUtils.loadMenuIcon(menu, R.id.rename_folder, IoniconsIcons.ion_edit, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.rename_folder) {
            renameFolderDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
