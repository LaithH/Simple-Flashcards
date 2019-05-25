package com.randomappsinc.simpleflashcards.quizlet.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletSetResult;

import java.util.List;

/** Utility class to do Quizlet searches, so UI pieces don't need to do any networking **/
public class QuizletSearchManager {

    public interface Listener {
        void onResultsFetched(List<QuizletSetResult> results, boolean paginationEnabled);
    }

    private static QuizletSearchManager instance;

    public static QuizletSearchManager getInstance() {
        if (instance == null) {
            instance = new QuizletSearchManager();
        }
        return instance;
    }

    @Nullable private Listener listener;
    private QuizletRestClient restClient;
    private boolean onlyShowImageSets;
    private boolean paginationEnabled;

    private QuizletSearchManager() {
        restClient = QuizletRestClient.getInstance();
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void performSearch(String searchTerm) {
        paginationEnabled = true;
        restClient.doFlashcardSetSearch(searchTerm, onlyShowImageSets ? 1 : 0);
    }

    public void fetchNewPage() {
        restClient.fetchNewPage();
    }

    public void onFlashcardSetsFound(List<QuizletSetResult> flashcardSets) {
        restClient.onFlashcardSetsFetched();
        // Disable pagination if we have fetched a set of flashcard sets that's less than the page size
        if (flashcardSets.size() < ApiConstants.PAGE_SIZE) {
            paginationEnabled = false;
        }
        if (listener != null) {
            listener.onResultsFetched(flashcardSets, paginationEnabled);
        }
    }

    public boolean getOnlyShowImageSets() {
        return onlyShowImageSets;
    }

    public void setOnlyShowImageSets(boolean onlyShowImageSets) {
        this.onlyShowImageSets = onlyShowImageSets;
    }

    public void clearEverything() {
        restClient.cancelFlashcardsSearch();
        listener = null;
    }
}
