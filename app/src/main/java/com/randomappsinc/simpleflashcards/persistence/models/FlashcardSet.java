package com.randomappsinc.simpleflashcards.persistence.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

public class FlashcardSet extends RealmObject {

    private int id;
    private long quizletSetId;

    @Required
    private String name;

    private RealmList<Flashcard> flashcards;

    @LinkingObjects("flashcardSets")
    private final RealmResults<FolderDO> folders = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getQuizletSetId() {
        return quizletSetId;
    }

    public void setQuizletSetId(long quizletSetId) {
        this.quizletSetId = quizletSetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Flashcard> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(RealmList<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public RealmResults<FolderDO> getFolders() {
        return folders;
    }
}
