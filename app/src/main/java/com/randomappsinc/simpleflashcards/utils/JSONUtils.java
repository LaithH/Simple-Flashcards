package com.randomappsinc.simpleflashcards.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class JSONUtils {

    private static final String QUIZLET_SET_ID_KEY = "quizletSetId";
    private static final String NAME_KEY = "name";
    private static final String FLASHCARDS_KEY = "flashcards";
    private static final String TERM_KEY = "term";
    private static final String TERM_IMAGE_URL_KEY = "termImageUrl";
    private static final String DEFINITION_KEY = "definition";
    private static final String POSITION_KEY = "position";

    public static String serializeFlashcardSet(FlashcardSetDO flashcardSet) {
        JSONObject flashcardSetJson = createFlashcardSetJson(flashcardSet);
        return flashcardSetJson == null ? "" : flashcardSetJson.toString();
    }

    public static String serializeFlashcardSets(List<FlashcardSetDO> flashcardSets) {
        JSONArray flashcardSetsArray = new JSONArray();
        for (FlashcardSetDO flashcardSet : flashcardSets) {
            JSONObject flashcardSetJson = createFlashcardSetJson(flashcardSet);
            if (flashcardSetJson != null) {
                flashcardSetsArray.put(flashcardSetJson);
            }
        }
        return flashcardSetsArray.toString();
    }

    @Nullable
    public static JSONObject createFlashcardSetJson(FlashcardSetDO flashcardSet) {
        try {
            JSONObject flashcardSetJson = new JSONObject();
            flashcardSetJson.put(QUIZLET_SET_ID_KEY, flashcardSet.getQuizletSetId());
            flashcardSetJson.put(NAME_KEY, flashcardSet.getName());

            JSONArray flashcards = new JSONArray();
            for (FlashcardDO flashcard : flashcardSet.getFlashcards()) {
                JSONObject flashcardJson = new JSONObject();
                flashcardJson.put(TERM_KEY, flashcard.getTerm());
                String termImageUrl = flashcard.getTermImageUrl();

                // Only transfer images with actual URLs since URIs won't work
                if (termImageUrl != null && termImageUrl.contains(Constants.QUIZLET_URL)) {
                    flashcardJson.put(TERM_IMAGE_URL_KEY, flashcard.getTermImageUrl());
                }

                flashcardJson.put(DEFINITION_KEY, flashcard.getDefinition());
                flashcardJson.put(POSITION_KEY, flashcard.getPosition());
                flashcards.put(flashcardJson);
            }
            flashcardSetJson.put(FLASHCARDS_KEY, flashcards);

            return flashcardSetJson;
        } catch (JSONException e) {
            return null;
        }
    }

    @Nullable
    public static FlashcardSetDO deserializeFlashcardSet(String flashcardSetJsonText) {
        if (TextUtils.isEmpty(flashcardSetJsonText)) {
            return null;
        }

        try {
            JSONObject flashcardSetJson = new JSONObject(flashcardSetJsonText);
            return getFlashcardSetFromJson(flashcardSetJson);
        } catch (JSONException ignored) {
            return null;
        }
    }

    public static FlashcardSetDO getFlashcardSetFromJson(JSONObject flashcardSetJson) {
        FlashcardSetDO flashcardSet = new FlashcardSetDO();
        try {
            flashcardSet.setQuizletSetId(flashcardSetJson.getLong(QUIZLET_SET_ID_KEY));
            flashcardSet.setName(flashcardSetJson.getString(NAME_KEY));

            RealmList<FlashcardDO> flashcards = new RealmList<>();
            JSONArray flashcardsArray = flashcardSetJson.getJSONArray(FLASHCARDS_KEY);
            for (int i = 0; i < flashcardsArray.length(); i++) {
                JSONObject flashcardJson = flashcardsArray.getJSONObject(i);
                FlashcardDO flashcard = new FlashcardDO();
                flashcard.setTerm(flashcardJson.getString(TERM_KEY));
                if (flashcardJson.has(TERM_IMAGE_URL_KEY)) {
                    flashcard.setTermImageUrl(flashcardJson.getString(TERM_IMAGE_URL_KEY));
                }
                flashcard.setDefinition(flashcardJson.getString(DEFINITION_KEY));
                if (flashcardJson.has(POSITION_KEY)) {
                    flashcard.setPosition(flashcardJson.getInt(POSITION_KEY));
                }
                flashcards.add(flashcard);
            }
            flashcardSet.setFlashcards(flashcards);
        }
        catch (JSONException ignored) {}
        return flashcardSet;
    }

    public static List<FlashcardSetDO> getSetsForDataRestoration(File backupFile) {
        String fileContents = FileUtils.getFileContents(backupFile);
        return deserializeSets(fileContents);
    }

    public static List<FlashcardSetDO> deserializeSets(String setsJson) {
        List<FlashcardSetDO> flashcardSets = new ArrayList<>();
        try {
            JSONArray setsArray = new JSONArray(setsJson);
            for (int i = 0; i < setsArray.length(); i++) {
                JSONObject flashcardSetJson = setsArray.getJSONObject(i);
                flashcardSets.add(getFlashcardSetFromJson(flashcardSetJson));
            }
        } catch (JSONException ignored) {}
        return flashcardSets;
    }
}
