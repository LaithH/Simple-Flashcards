package com.randomappsinc.simpleflashcards.quizlet.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QuizletImage {

    @SerializedName("url")
    @Expose
    private String url;

    String getUrl() {
        return url;
    }
}
