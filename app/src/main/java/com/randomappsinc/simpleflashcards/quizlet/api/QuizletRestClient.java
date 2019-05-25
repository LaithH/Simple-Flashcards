package com.randomappsinc.simpleflashcards.quizlet.api;

import android.os.Handler;
import android.os.HandlerThread;

import com.randomappsinc.simpleflashcards.quizlet.api.callbacks.FetchFlashcardSetCallback;
import com.randomappsinc.simpleflashcards.quizlet.api.callbacks.FindFlashcardSetsCallback;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletFlashcardSet;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletSearchResults;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizletRestClient {

    private static QuizletRestClient instance;

    protected QuizletService quizletService;
    private Handler handler;
    private boolean isPaginating = false;
    private String currentSearchTerm;
    private int imageSetsOnly;
    private int pageToFetch;
    protected Call<QuizletSearchResults> currentFindFlashcardSetsCall;
    protected Call<QuizletFlashcardSet> currentFetchSetCall;

    public static QuizletRestClient getInstance() {
        if (instance == null) {
            instance = new QuizletRestClient();
        }
        return instance;
    }

    private QuizletRestClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new QuizletAuthInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        quizletService = retrofit.create(QuizletService.class);

        HandlerThread backgroundThread = new HandlerThread("");
        backgroundThread.start();
        handler = new Handler(backgroundThread.getLooper());
    }

    void doFlashcardSetSearch(final String searchTerm, final int imageSetsOnly) {
        isPaginating = false;
        pageToFetch = 1;
        currentSearchTerm = searchTerm;
        this.imageSetsOnly = imageSetsOnly;
        cancelFlashcardsSearch();
        currentFindFlashcardSetsCall = quizletService.findFlashcardSets(
                searchTerm,
                imageSetsOnly,
                pageToFetch,
                ApiConstants.PAGE_SIZE);
        currentFindFlashcardSetsCall.enqueue(new FindFlashcardSetsCallback());
    }

    void fetchNewPage() {
        if (!isPaginating) {
            isPaginating = true;
            currentFindFlashcardSetsCall = quizletService.findFlashcardSets(
                    currentSearchTerm,
                    imageSetsOnly,
                    pageToFetch,
                    ApiConstants.PAGE_SIZE);
            currentFindFlashcardSetsCall.enqueue(new FindFlashcardSetsCallback());
        }
    }

    void onFlashcardSetsFetched() {
        isPaginating = false;
        pageToFetch++;
    }

    void cancelFlashcardsSearch() {
        handler.post(() -> {
            if (currentFindFlashcardSetsCall != null) {
                currentFindFlashcardSetsCall.cancel();
            }
        });
    }

    void fetchFlashcardSet(final long setId) {
        handler.post(() -> {
            if (currentFetchSetCall != null) {
                currentFetchSetCall.cancel();
            }
            currentFetchSetCall = quizletService.getFlashcardSetInfo(setId);
            currentFetchSetCall.enqueue(new FetchFlashcardSetCallback());
        });
    }

    void cancelFlashcardSetFetch() {
        handler.post(() -> {
            if (currentFetchSetCall != null) {
                currentFetchSetCall.cancel();
            }
        });
    }
}
