package com.randomappsinc.simpleflashcards.folders.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.folders.activities.FolderActivity;
import com.randomappsinc.simpleflashcards.folders.adapters.FoldersAdapter;
import com.randomappsinc.simpleflashcards.folders.dialogs.CreateFolderDialog;
import com.randomappsinc.simpleflashcards.folders.dialogs.DeleteFolderDialog;
import com.randomappsinc.simpleflashcards.folders.models.Folder;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.utils.StringUtils;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

public class FoldersFragment extends Fragment
        implements CreateFolderDialog.Listener, FoldersAdapter.Listener, DeleteFolderDialog.Listener {

    private static final int SPEECH_REQUEST_CODE = 4;

    public static FoldersFragment newInstance() {
        return new FoldersFragment();
    }

    @BindView(R.id.focus_sink) View focusSink;
    @BindView(R.id.search_bar) View searchBar;
    @BindView(R.id.search_input) EditText searchInput;
    @BindView(R.id.voice_search) View voiceSearch;
    @BindView(R.id.clear_search) View clearSearch;

    @BindView(R.id.add_folder) FloatingActionButton addFolder;
    @BindView(R.id.no_folders) TextView noFolders;
    @BindView(R.id.folders_list_container) View listContainer;
    @BindView(R.id.folders) RecyclerView folders;

    private CreateFolderDialog createFolderDialog;
    private DeleteFolderDialog deleteFolderDialog;
    private DatabaseManager databaseManager = DatabaseManager.get();
    private FoldersAdapter adapter;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.folders,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);

        addFolder.setImageDrawable(new IconDrawable(getContext(), IoniconsIcons.ion_android_add)
                .colorRes(R.color.white)
                .actionBarSize());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createFolderDialog = new CreateFolderDialog(getActivity(), this);
        deleteFolderDialog = new DeleteFolderDialog(getActivity(), this);
        adapter = new FoldersAdapter(this);
        folders.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        folders.setAdapter(adapter);

        // When the user is scrolling, close the soft keyboard
        folders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    UIUtils.closeKeyboard(getActivity());
                    takeAwayFocusFromSearch();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.refreshContent(searchInput.getText().toString());
        updateNoFoldersVisibility();
    }

    @OnTextChanged(value = R.id.search_input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable input) {
        adapter.refreshContent(input.toString());
        voiceSearch.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        clearSearch.setVisibility(input.length() == 0 ? View.GONE : View.VISIBLE);
        updateNoFoldersVisibility();
    }

    @OnClick(R.id.clear_search)
    public void clearSearch() {
        searchInput.setText("");
    }

    @OnClick(R.id.voice_search)
    public void searchWithVoice() {
        showGoogleSpeechDialog();
    }

    private void showGoogleSpeechDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_message));
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
            getActivity().overridePendingTransition(R.anim.stay, R.anim.slide_in_bottom);
        } catch (ActivityNotFoundException exception) {
            UIUtils.showLongToast(R.string.speech_not_supported, getContext());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_REQUEST_CODE:
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result == null || result.isEmpty()) {
                    UIUtils.showLongToast(R.string.speech_unrecognized, getContext());
                    return;
                }
                String voiceQuery = StringUtils.capitalizeFirstWord(result.get(0));
                searchInput.setText(voiceQuery);
                break;
        }
    }

    // Stop the EditText cursor from blinking
    protected void takeAwayFocusFromSearch() {
        focusSink.requestFocus();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        takeAwayFocusFromSearch();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            adapter.refreshContent(searchInput.getText().toString());
            updateNoFoldersVisibility();
        }
    }

    public void updateNoFoldersVisibility() {
        int numFoldersInDb = databaseManager.getNumFolders();
        if (numFoldersInDb == 0) {
            searchBar.setVisibility(View.GONE);
            listContainer.setVisibility(View.GONE);
            noFolders.setText(R.string.no_folders_text);
            noFolders.setVisibility(View.VISIBLE);
        } else {
            searchBar.setVisibility(View.VISIBLE);
            if (adapter.getItemCount() == 0) {
                listContainer.setVisibility(View.GONE);
                noFolders.setText(R.string.no_folders_from_search);
                noFolders.setVisibility(View.VISIBLE);
            } else {
                listContainer.setVisibility(View.VISIBLE);
                noFolders.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onFolderClicked(int folderId) {
        Intent intent = new Intent(getActivity(), FolderActivity.class)
                .putExtra(Constants.FOLDER_ID_KEY, folderId);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void onFolderDeleteRequested(Folder folder) {
        deleteFolderDialog.show(folder);
    }

    @Override
    public void onFolderDeleted(Folder folder) {
        databaseManager.deleteFolder(folder);
        adapter.onFolderDeleted();
        updateNoFoldersVisibility();
    }

    @OnClick(R.id.add_folder)
    public void addFolder() {
        createFolderDialog.show();
    }

    @Override
    public void onNewFolderSubmitted(String folderName) {
        int newFolderId = databaseManager.createFolder(folderName);
        adapter.refreshContent(searchInput.getText().toString());
        updateNoFoldersVisibility();
        Intent intent = new Intent(getActivity(), FolderActivity.class)
                .putExtra(Constants.FOLDER_ID_KEY, newFolderId);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.cleanup();
        createFolderDialog.cleanUp();
        deleteFolderDialog.cleanUp();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.sort_flashcard_sets).setVisible(false);
        menu.findItem(R.id.filter).setVisible(false);
    }
}
