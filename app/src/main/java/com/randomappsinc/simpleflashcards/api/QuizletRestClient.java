package com.randomappsinc.simpleflashcards.api;

import android.os.Handler;
import android.os.HandlerThread;

import com.randomappsinc.simpleflashcards.api.callbacks.FindFlashcardSetsCallback;
import com.randomappsinc.simpleflashcards.api.models.QuizletSearchResults;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizletRestClient {

    private static QuizletRestClient instance;

    protected QuizletService quizletService;
    private Handler handler;
    protected Call<QuizletSearchResults> currentFindFlashcardSetsCall;

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

    void findFlashcardSets(final String searchTerm) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (currentFindFlashcardSetsCall != null) {
                    currentFindFlashcardSetsCall.cancel();
                }
                currentFindFlashcardSetsCall = quizletService.findFlashcardSets(searchTerm);
                currentFindFlashcardSetsCall.enqueue(new FindFlashcardSetsCallback());
            }
        });
    }

    void cancelFlashcardsSearch() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (currentFindFlashcardSetsCall != null) {
                    currentFindFlashcardSetsCall.cancel();
                }
            }
        });
    }
}
