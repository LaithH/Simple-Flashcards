package com.randomappsinc.simpleflashcards.quizlet.api;

import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletFlashcardSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Utility class to fetch an entire Quizlet flashcard set, so UI pieces don't need to do any networking **/
public class QuizletFlashcardSetFetcher {

    public interface Listener {
        void onFlashcardSetFetched(QuizletFlashcardSet flashcardSet);
    }

    private static QuizletFlashcardSetFetcher instance;

    public static QuizletFlashcardSetFetcher getInstance() {
        if (instance == null) {
            instance = new QuizletFlashcardSetFetcher();
        }
        return instance;
    }

    @Nullable private Listener listener;
    private QuizletRestClient restClient;

    private QuizletFlashcardSetFetcher() {
        restClient = QuizletRestClient.getInstance();
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void fetchSet(long setId) {
        restClient.fetchFlashcardSet(setId);
    }

    public void onFlashcardSetFetched(QuizletFlashcardSet flashcardSet) {
        if (listener != null) {
            listener.onFlashcardSetFetched(flashcardSet);
        }
    }

    public void clearEverything() {
        restClient.cancelFlashcardSetFetch();
        listener = null;
    }
}
