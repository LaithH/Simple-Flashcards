package com.randomappsinc.simpleflashcards.quizlet.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.quizlet.activities.QuizletSearchFilterActivity;
import com.randomappsinc.simpleflashcards.quizlet.activities.QuizletSetViewActivity;
import com.randomappsinc.simpleflashcards.quizlet.adapters.QuizletSearchResultsAdapter;
import com.randomappsinc.simpleflashcards.quizlet.api.QuizletSearchManager;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletSetResult;
import com.randomappsinc.simpleflashcards.utils.StringUtils;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

public class QuizletSearchFragment extends Fragment
        implements QuizletSearchManager.Listener, QuizletSearchResultsAdapter.Listener {

    public static QuizletSearchFragment newInstance() {
        return new QuizletSearchFragment();
    }

    private static final int SPEECH_REQUEST_CODE = 2;
    private static final int FILTER_REQUEST_CODE = 3;

    private static final long MILLIS_DELAY_FOR_KEYBOARD = 150;

    @BindView(R.id.parent) View parent;
    @BindView(R.id.search_input) EditText setSearch;
    @BindView(R.id.voice_search) View voiceSearch;
    @BindView(R.id.clear_search) View clearSearch;
    @BindView(R.id.search_empty_text) TextView searchEmptyText;
    @BindView(R.id.quizlet_attribution) View quizletAttribution;
    @BindView(R.id.skeleton_results) View skeletonResults;
    @BindView(R.id.search_results) RecyclerView searchResults;

    protected QuizletSearchResultsAdapter adapter;
    private QuizletSearchManager searchManager;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.quizlet_search,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        searchManager = QuizletSearchManager.getInstance();
        searchManager.setListener(this);

        adapter = new QuizletSearchResultsAdapter(getActivity(), this);
        searchResults.setAdapter(adapter);
        searchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    UIUtils.closeKeyboard(getActivity());
                    parent.requestFocus();
                }
            }
        });

        if (setSearch.requestFocus()) {
            setSearch.postDelayed(() -> {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                InputMethodManager inputMethodManager =
                        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager == null) {
                    return;
                }
                inputMethodManager.showSoftInput(setSearch, InputMethodManager.SHOW_IMPLICIT);
            }, MILLIS_DELAY_FOR_KEYBOARD);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @OnTextChanged(value = R.id.search_input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable input) {
        adapter.clear();
        searchResults.setVisibility(View.GONE);
        searchResults.scrollToPosition(0);
        skeletonResults.setVisibility(input.length() == 0 ? View.GONE : View.VISIBLE);

        if (input.length() == 0) {
            searchManager.cancelCurrentSearch();
            searchEmptyText.setText(R.string.quizlet_search_empty_state);
        }
        searchEmptyText.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        quizletAttribution.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        voiceSearch.setVisibility(input.length() == 0 ? View.VISIBLE : View.GONE);
        clearSearch.setVisibility(input.length() == 0 ? View.GONE : View.VISIBLE);

        if (input.length() > 0) {
            searchManager.performSearch(input.toString());
        }
    }

    @OnClick(R.id.clear_search)
    public void clearSearch() {
        searchManager.cancelCurrentSearch();
        setSearch.setText("");
    }

    @Override
    public void onResultsFetched(List<QuizletSetResult> results, boolean paginationEnabled) {
        skeletonResults.setVisibility(View.GONE);
        adapter.addFlashcardSets(results, paginationEnabled);
        if (adapter.getItemCount() == 0) {
            searchEmptyText.setText(R.string.no_quizlet_results);
            searchEmptyText.setVisibility(View.VISIBLE);
        } else {
            searchEmptyText.setVisibility(View.GONE);
            searchResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResultClicked(QuizletSetResult result) {
        Intent intent = new Intent(
                getActivity(), QuizletSetViewActivity.class)
                .putExtra(Constants.QUIZLET_SET_ID, result.getQuizletSetId())
                .putExtra(Constants.QUIZLET_SET_TITLE, result.getTitle());
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
    }

    @Override
    public void onPaginationSpinnerLoaded() {
        searchManager.fetchNewPage();
    }

    @OnClick(R.id.voice_search)
    public void searchWithVoice() {
        parent.requestFocus();
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
            case FILTER_REQUEST_CODE:
                String input = setSearch.getText().toString();
                if (resultCode == Activity.RESULT_OK && !input.isEmpty()) {
                    searchManager.performSearch(input);
                    searchResults.setVisibility(View.GONE);
                    searchResults.scrollToPosition(0);
                    skeletonResults.setVisibility(View.VISIBLE);
                }
                break;
            case SPEECH_REQUEST_CODE:
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
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchManager.clearEverything();
        adapter.cleanUp();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.filter).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter) {
            startActivityForResult(
                    new Intent(getActivity(), QuizletSearchFilterActivity.class),
                    FILTER_REQUEST_CODE);
            getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
