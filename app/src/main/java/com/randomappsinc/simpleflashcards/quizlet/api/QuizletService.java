package com.randomappsinc.simpleflashcards.quizlet.api;

import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletFlashcardSet;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletSearchResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface QuizletService {

    @GET("search/sets")
    Call<QuizletSearchResults> findFlashcardSets(
            @Query("q") String term,
            @Query("images_only") int imagesOnly,
            @Query("page") int page,
            @Query("per_page") int pageSize);

    @GET("sets/{id}")
    Call<QuizletFlashcardSet> getFlashcardSetInfo(@Path("id") long setId);
}
