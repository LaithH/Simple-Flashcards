package com.randomappsinc.simpleflashcards.home.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.backupandrestore.activities.BackupAndRestoreActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.csvimport.CsvImportActivity;
import com.randomappsinc.simpleflashcards.editflashcards.activities.EditFlashcardsActivity;
import com.randomappsinc.simpleflashcards.home.activities.FlashcardSetActivity;
import com.randomappsinc.simpleflashcards.home.activities.MainActivity;
import com.randomappsinc.simpleflashcards.home.adapters.HomepageFlashcardSetsAdapter;
import com.randomappsinc.simpleflashcards.home.dialogs.CreateFlashcardSetDialog;
import com.randomappsinc.simpleflashcards.home.dialogs.SortFlashcardSetsDialog;
import com.randomappsinc.simpleflashcards.nearbysharing.activities.NearbySharingActivity;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.utils.FlashcardUtils;
import com.randomappsinc.simpleflashcards.utils.StringUtils;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

public class HomepageFragment extends Fragment
        implements HomepageFlashcardSetsAdapter.Listener, CreateFlashcardSetDialog.Listener,
        SortFlashcardSetsDialog.Listener {

    public static HomepageFragment newInstance() {
        return new HomepageFragment();
    }

    private static final int SPEECH_REQUEST_CODE = 1;
    private static final int CSV_IMPORT_REQUEST_CODE = 2;

    @BindView(R.id.parent) View parent;
    @BindView(R.id.focus_sink) View focusSink;
    @BindView(R.id.search_bar) View searchBar;
    @BindView(R.id.search_input) EditText setSearch;
    @BindView(R.id.voice_search) View voiceSearch;
    @BindView(R.id.clear_search) View clearSearch;

    // Contains both the list and the fade it has
    @BindView(R.id.sets_list_container) View setsContainer;

    @BindView(R.id.flashcard_sets) RecyclerView sets;
    @BindView(R.id.no_sets) View noSetsAtAll;
    @BindView(R.id.no_sets_match) View noSetsMatch;
    @BindView(R.id.add_flashcard_set) FloatingActionButton addFlashcardSet;

    protected HomepageFlashcardSetsAdapter adapter;
    private CreateFlashcardSetDialog createFlashcardSetDialog;
    private DatabaseManager databaseManager = DatabaseManager.get();
    private SortFlashcardSetsDialog sortFlashcardSetsDialog;
    @Nullable private Comparator<FlashcardSetDO> setComparator;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.homepage,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);
        addFlashcardSet.setImageDrawable(new IconDrawable(getContext(), IoniconsIcons.ion_android_add)
                .colorRes(R.color.white)
                .actionBarSize());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createFlashcardSetDialog = new CreateFlashcardSetDialog(getActivity(), this);

        adapter = new HomepageFlashcardSetsAdapter(this);
        sets.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        sets.setAdapter(adapter);

        // When the user is scrolling to browse flashcards, close the soft keyboard
        sets.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    // Stop the EditText cursor from blinking
    protected void takeAwayFocusFromSearch() {
        focusSink.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.refreshContent(setSearch.getText().toString(), setComparator);
    }

    @OnTextChanged(value = R.id.search_input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable input) {
        adapter.refreshContent(input.toString(), setComparator);
        voiceSearch.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        clearSearch.setVisibility(input.length() == 0 ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.clear_search)
    public void clearSearch() {
        setSearch.setText("");
    }

    @OnClick(R.id.add_flashcard_set)
    public void addSet() {
        createFlashcardSetDialog.show();
    }

    @OnClick(R.id.download_sets_button)
    public void downloadFlashcards() {
        MainActivity activity = (MainActivity) getActivity();
        activity.loadQuizletSetSearch();
    }

    @OnClick(R.id.import_from_csv_button)
    public void importFromCsv() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            UIUtils.showLongToast(R.string.csv_format_instructions, getContext());
            Intent csvIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            csvIntent.addCategory(Intent.CATEGORY_OPENABLE);
            csvIntent.setType("*/*");
            csvIntent.putExtra(Intent.EXTRA_MIME_TYPES, Constants.CSV_MIME_TYPES);
            startActivityForResult(csvIntent, CSV_IMPORT_REQUEST_CODE);
        }
    }

    @OnClick(R.id.create_set_button)
    public void createSet() {
        createFlashcardSetDialog.show();
    }

    @OnClick(R.id.restore_sets_button)
    public void restoreSets() {
        Intent intent = new Intent(getActivity(), BackupAndRestoreActivity.class)
                .putExtra(Constants.GO_TO_RESTORE_IMMEDIATELY_KEY, true);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @OnClick(R.id.share_with_nearby_button)
    public void shareWithNearby() {
        startActivity(new Intent(getActivity(), NearbySharingActivity.class));
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void onFlashcardSetCreated(String newSetName) {
        int newSetId = databaseManager.createFlashcardSet(newSetName);
        adapter.refreshContent(setSearch.getText().toString(), setComparator);
        Intent intent = new Intent(getActivity(), EditFlashcardsActivity.class);
        intent.putExtra(Constants.FLASHCARD_SET_ID_KEY, newSetId);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void onFlashcardSetClicked(FlashcardSetDO flashcardSetDO) {
        Intent intent = new Intent(getActivity(), FlashcardSetActivity.class);
        intent.putExtra(Constants.FLASHCARD_SET_ID_KEY, flashcardSetDO.getId());
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void onContentUpdated(int numSets) {
        if (databaseManager.getNumFlashcardSets() == 0) {
            searchBar.setVisibility(View.GONE);
            setsContainer.setVisibility(View.GONE);
            noSetsMatch.setVisibility(View.GONE);
            noSetsAtAll.setVisibility(View.VISIBLE);
        } else {
            noSetsAtAll.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            if (numSets == 0) {
                setsContainer.setVisibility(View.GONE);
                noSetsMatch.setVisibility(View.VISIBLE);
            } else {
                noSetsMatch.setVisibility(View.GONE);
                setsContainer.setVisibility(View.VISIBLE);
            }
        }
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
    public void onSortAlphabeticalAscending() {
        setComparator = (set1, set2) -> set1.getName().toLowerCase().compareTo(set2.getName().toLowerCase());
        adapter.refreshContent(setSearch.getText().toString(), setComparator);
        sets.scrollToPosition(0);
        UIUtils.showShortToast(R.string.sort_applied, getContext());
    }

    @Override
    public void onSortAlphabeticalDescending() {
        setComparator = (set1, set2) -> set2.getName().toLowerCase().compareTo(set1.getName().toLowerCase());
        adapter.refreshContent(setSearch.getText().toString(), setComparator);
        sets.scrollToPosition(0);
        UIUtils.showShortToast(R.string.sort_applied, getContext());
    }

    @Override
    public void onLeastLearnedFirst() {
        setComparator = (set1, set2) -> {
            double firstLearnedPercent = FlashcardUtils.getLearnedPercent(set1);
            double secondLearnedPercent = FlashcardUtils.getLearnedPercent(set2);
            if (firstLearnedPercent > secondLearnedPercent) {
                return 1;
            } else if (firstLearnedPercent == secondLearnedPercent) {
                return 0;
            }
            return -1;
        };
        adapter.refreshContent(setSearch.getText().toString(), setComparator);
        sets.scrollToPosition(0);
        UIUtils.showShortToast(R.string.sort_applied, getContext());
    }

    @Override
    public void onMostLearnedFirst() {
        setComparator = (set1, set2) -> {
            double firstLearnedPercent = FlashcardUtils.getLearnedPercent(set1);
            double secondLearnedPercent = FlashcardUtils.getLearnedPercent(set2);
            if (firstLearnedPercent > secondLearnedPercent) {
                return -1;
            } else if (firstLearnedPercent == secondLearnedPercent) {
                return 0;
            }
            return 1;
        };
        adapter.refreshContent(setSearch.getText().toString(), setComparator);
        sets.scrollToPosition(0);
        UIUtils.showShortToast(R.string.sort_applied, getContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK || data == null) {
                return;
            }
            List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result == null || result.isEmpty()) {
                UIUtils.showLongToast(R.string.speech_unrecognized, getContext());
                return;
            }
            String searchInput = StringUtils.capitalizeFirstWord(result.get(0));
            setSearch.setText(searchInput);
        } else if (requestCode == CSV_IMPORT_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && resultCode == Activity.RESULT_OK
                    && data != null && data.getData() != null) {
                Uri uri = data.getData();

                // Persist ability to read from this file
                int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

                String uriString = uri.toString();
                Intent intent = new Intent(getActivity(), CsvImportActivity.class);
                intent.putExtra(Constants.URI_KEY, uriString);
                startActivity(intent);
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        takeAwayFocusFromSearch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.cleanup();
        createFlashcardSetDialog.cleanUp();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.sort_flashcard_sets).setVisible(true);
        menu.findItem(R.id.filter).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sort_flashcard_sets) {
            if (sortFlashcardSetsDialog == null) {
                sortFlashcardSetsDialog = new SortFlashcardSetsDialog(getContext(), this);
            }
            sortFlashcardSetsDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
