package com.randomappsinc.simpleflashcards.common.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;

public class Flashcard implements Parcelable {

    private int id;
    private String term;
    private String definition;
    private String termImageUrl;
    private String definitionImageUrl;
    private boolean learned;

    public Flashcard(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public Flashcard(FlashcardDO flashcardDO) {
        this.id = flashcardDO.getId();
        this.term = flashcardDO.getTerm();
        this.definition = flashcardDO.getDefinition();
        this.termImageUrl = flashcardDO.getTermImageUrl();
        this.definitionImageUrl = flashcardDO.getDefinitionImageUrl();
        this.learned = flashcardDO.isLearned();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getTermImageUrl() {
        return termImageUrl;
    }

    public void setTermImageUrl(String termImageUrl) {
        this.termImageUrl = termImageUrl;
    }

    public String getDefinitionImageUrl() {
        return definitionImageUrl;
    }

    public void setDefinitionImageUrl(String definitionImageUrl) {
        this.definitionImageUrl = definitionImageUrl;
    }

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }

    protected Flashcard(Parcel in) {
        id = in.readInt();
        term = in.readString();
        definition = in.readString();
        termImageUrl = in.readString();
        definitionImageUrl = in.readString();
        learned = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(term);
        dest.writeString(definition);
        dest.writeString(termImageUrl);
        dest.writeString(definitionImageUrl);
        dest.writeByte((byte) (learned ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Flashcard> CREATOR = new Parcelable.Creator<Flashcard>() {
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };
}