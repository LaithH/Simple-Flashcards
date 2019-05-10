package com.randomappsinc.simpleflashcards.quizlet.api.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QuizletFlashcard {

    @SerializedName("term")
    @Expose
    private String term;

    @SerializedName("definition")
    @Expose
    private String definition;

    @SerializedName("image")
    @Expose
    private QuizletImage image;

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    @Nullable
    public String getImageUrl() {
        return image == null ? null : image.getUrl();
    }
}
