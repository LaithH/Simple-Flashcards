package com.randomappsinc.simpleflashcards.persistence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.common.constants.Language;
import com.randomappsinc.simpleflashcards.common.models.Flashcard;
import com.randomappsinc.simpleflashcards.common.models.FlashcardSetPreview;
import com.randomappsinc.simpleflashcards.folders.models.Folder;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.persistence.models.FolderDO;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletFlashcard;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletFlashcardSet;
import com.randomappsinc.simpleflashcards.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Case;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class DatabaseManager {

    public interface Listener {
        void onDatabaseUpdated();
    }

    private static final int CURRENT_REALM_VERSION = 10;

    private static DatabaseManager instance;

    public static DatabaseManager get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized DatabaseManager getSync() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private Realm realm;
    protected boolean idMigrationNeeded;
    @Nullable protected Listener listener;

    private DatabaseManager() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .schemaVersion(CURRENT_REALM_VERSION)
                .migration(migration)
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();

        if (idMigrationNeeded) {
            addIdsToEverything();
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        if (listener == null) {
            realm.removeChangeListener(realmChangeListener);
        } else {
            realm.addChangeListener(realmChangeListener);
        }
    }

    private final RealmChangeListener<Realm> realmChangeListener =
            new RealmChangeListener<Realm>() {
                @Override
                public void onChange(@NonNull Realm realm) {
                    if (listener != null) {
                        listener.onDatabaseUpdated();
                    }
                }
            };

    private final RealmMigration migration = new RealmMigration() {
        @Override
        public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();

            // Remove flashcard positioning
            if (oldVersion == 0) {
                RealmObjectSchema setSchema = schema.get("FlashcardSet");
                if (setSchema != null) {
                    setSchema.removePrimaryKey();
                    setSchema.removeField("position");
                }
                oldVersion++;
            }

            // Add IDs to objects
            if (oldVersion == 1) {
                RealmObjectSchema setSchema = schema.get("FlashcardSet");
                if (setSchema != null) {
                    setSchema.addField("id", int.class);
                } else {
                    throw new IllegalStateException("FlashcardSet schema doesn't exist.");
                }
                RealmObjectSchema cardSchema = schema.get("Flashcard");
                if (cardSchema != null) {
                    cardSchema.addField("id", int.class);
                } else {
                    throw new IllegalStateException("Flashcard schema doesn't exist.");
                }
                idMigrationNeeded = true;
                oldVersion++;
            }

            // Rename "question" and "answer" to "term" and "definition"
            if (oldVersion == 2) {
                RealmObjectSchema cardSchema = schema.get("Flashcard");
                if (cardSchema != null) {
                    cardSchema.renameField("question", "term");
                    cardSchema.renameField("answer", "definition");
                } else {
                    throw new IllegalStateException("Flashcard schema doesn't exist.");
                }
                oldVersion++;
            }

            // Rename "question" and "answer" to "term" and "definition"
            if (oldVersion == 3) {
                RealmObjectSchema setSchema = schema.get("FlashcardSet");
                if (setSchema != null) {
                    setSchema.addField("quizletSetId", long.class);
                } else {
                    throw new IllegalStateException("FlashcardSet schema doesn't exist.");
                }
                RealmObjectSchema cardSchema = schema.get("Flashcard");
                if (cardSchema != null) {
                    cardSchema.addField("termImageUrl", String.class);
                } else {
                    throw new IllegalStateException("Flashcard schema doesn't exist.");
                }
                oldVersion++;
            }

            // Add folder support
            if (oldVersion == 4) {
                RealmObjectSchema folderSchema = schema.create("FolderDO")
                        .addField("id", int.class)
                        .addField("name", String.class);
                RealmObjectSchema setSchema = schema.get("FlashcardSet");
                if (setSchema != null) {
                    folderSchema.addRealmListField("flashcardSets", setSchema);
                } else {
                    throw new IllegalStateException("FlashcardSet doesn't exist.");
                }
                oldVersion++;
            }

            // Add image support for definition
            if (oldVersion == 5) {
                RealmObjectSchema cardSchema = schema.get("Flashcard");
                if (cardSchema != null) {
                    cardSchema.addField("definitionImageUrl", String.class);
                } else {
                    throw new IllegalStateException("Flashcard schema doesn't exist.");
                }
                oldVersion++;
            }

            // Add ability to mark flashcards as learned
            if (oldVersion == 6) {
                RealmObjectSchema cardSchema = schema.get("Flashcard");
                if (cardSchema != null) {
                    cardSchema.addField("learned", boolean.class);
                } else {
                    throw new IllegalStateException("Flashcard schema doesn't exist.");
                }
                oldVersion++;
            }

            // Move FlashcardSetDO and FlashcardDO to DO naming
            if (oldVersion == 7) {
                schema.rename("FlashcardSet", "FlashcardSetDO");
                schema.rename("Flashcard", "FlashcardDO");
                oldVersion++;
            }

            // Add terms and definitions language setting support
            if (oldVersion == 8) {
                RealmObjectSchema setSchema = schema.get("FlashcardSetDO");
                if (setSchema != null) {
                    setSchema.addField("termsLanguage", int.class);
                    setSchema.addField("definitionsLanguage", int.class);
                } else {
                    throw new IllegalStateException("FlashcardSetDO schema doesn't exist.");
                }
                oldVersion++;
            }

            // Add flashcard position
            if (oldVersion == 9) {
                RealmObjectSchema flashcardSchema = schema.get("FlashcardDO");
                if (flashcardSchema != null) {
                    flashcardSchema.addField("position", int.class);
                } else {
                    throw new IllegalStateException("FlashcardDO schema doesn't exist.");
                }
            }
        }
    };

    private void addIdsToEverything() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                List<FlashcardSetDO> flashcardSets = realm.where(FlashcardSetDO.class).findAll();
                for (FlashcardSetDO flashcardSet : flashcardSets) {
                    flashcardSet.setId(getNextSetId());
                    for (FlashcardDO flashcard : flashcardSet.getFlashcards()) {
                        flashcard.setId(getNextFlashcardId());
                    }
                }
            }
        });
    }

    public int createFlashcardSet(String setName) {
        try {
            realm.beginTransaction();
            FlashcardSetDO set = new FlashcardSetDO();
            int newSetId = getNextSetId();
            set.setId(newSetId);
            set.setName(setName);
            realm.copyToRealm(set);
            realm.commitTransaction();
            return newSetId;
        } catch (Exception e) {
            realm.cancelTransaction();
        }
        return 0;
    }

    protected int getNextSetId() {
        Number number = realm.where(FlashcardSetDO.class).findAll().max("id");
        return number == null ? 1 : number.intValue() + 1;
    }

    protected int getNextFlashcardId() {
        Number number = realm.where(FlashcardDO.class).findAll().max("id");
        return number == null ? 1 : number.intValue() + 1;
    }

    public void renameSet(int setId, String newName) {
        try {
            realm.beginTransaction();
            FlashcardSetDO set = realm.where(FlashcardSetDO.class)
                    .equalTo("id", setId)
                    .findFirst();
            set.setName(newName);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void addFlashcard(int setId, String term, String definition) {
        try {
            realm.beginTransaction();
            FlashcardSetDO set = realm.where(FlashcardSetDO.class).equalTo("id", setId).findFirst();
            FlashcardDO flashcard = new FlashcardDO();
            flashcard.setId(getNextFlashcardId());
            flashcard.setTerm(term);
            flashcard.setDefinition(definition);
            if (set.getFlashcards() == null || set.getFlashcards().isEmpty()) {
                flashcard.setPosition(0);
            } else {
                Number currentMaxPosition = set.getFlashcards().max("position");
                flashcard.setPosition(currentMaxPosition.intValue());
            }
            set.getFlashcards().add(flashcard);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void updateFlashcardTerm(int flashcardId, String newTerm) {
        try {
            realm.beginTransaction();
            FlashcardDO flashcard = realm.where(FlashcardDO.class)
                    .equalTo("id", flashcardId)
                    .findFirst();
            flashcard.setTerm(newTerm);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void updateFlashcardDefinition(int flashcardId, String newDefinition) {
        try {
            realm.beginTransaction();
            FlashcardDO flashcard = realm.where(FlashcardDO.class)
                    .equalTo("id", flashcardId)
                    .findFirst();
            flashcard.setDefinition(newDefinition);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void updateFlashcardTermImageUrl(int flashcardId, String imageUrl) {
        try {
            realm.beginTransaction();
            FlashcardDO flashcard = realm.where(FlashcardDO.class)
                    .equalTo("id", flashcardId)
                    .findFirst();
            String currentImagePath = flashcard.getTermImageUrl();
            FileUtils.maybeDeleteFileWithUri(currentImagePath);
            flashcard.setTermImageUrl(imageUrl);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void updateFlashcardDefinitionImageUrl(int flashcardId, String imageUrl) {
        try {
            realm.beginTransaction();
            FlashcardDO flashcard = realm.where(FlashcardDO.class)
                    .equalTo("id", flashcardId)
                    .findFirst();
            String currentImagePath = flashcard.getDefinitionImageUrl();
            FileUtils.maybeDeleteFileWithUri(currentImagePath);
            flashcard.setDefinitionImageUrl(imageUrl);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void deleteFlashcard(int flashcardId) {
        try {
            realm.beginTransaction();
            FlashcardDO flashcard = realm
                    .where(FlashcardDO.class)
                    .equalTo("id", flashcardId)
                    .findFirst();
            flashcard.deleteFromRealm();
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void deleteFlashcardSet(int setId) {
        try {
            realm.beginTransaction();
            FlashcardSetDO setToRemove = realm
                    .where(FlashcardSetDO.class)
                    .equalTo("id", setId)
                    .findFirst();
            setToRemove.deleteFromRealm();
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    /**
     * Gets all flashcard sets which contain the given search term, case insensitive.
     * If the search term is empty, return all flashcard sets.
     */
    public List<FlashcardSetDO> getFlashcardSets(String searchTerm) {
        List<FlashcardSetDO> flashcardSets;
        if (searchTerm.trim().isEmpty()) {
            flashcardSets = realm.where(FlashcardSetDO.class).findAll();
        } else {
            flashcardSets = realm
                    .where(FlashcardSetDO.class)
                    .contains("name", searchTerm, Case.INSENSITIVE)
                    .findAll();
        }
        return flashcardSets;
    }

    public List<FlashcardSetDO> getNonEmptyFlashcardSets(int setIdToExclude) {
        return realm.where(FlashcardSetDO.class)
                .isNotEmpty("flashcards")
                .notEqualTo("id", setIdToExclude)
                .findAll();
    }

    /**
     * Creates a deep copy of all flashcard sets to avoid Realm access shenanigans.
     */
    public List<FlashcardSetDO> getAllFlashcardSetsOnAnyThread() {
        return Realm.getDefaultInstance().where(FlashcardSetDO.class).findAll();
    }

    public List<FlashcardDO> getAllFlashcards(int setId) {
        RealmResults<FlashcardDO> flashcards = realm.where(FlashcardSetDO.class)
                .equalTo("id", setId)
                .findFirst()
                .getFlashcards()
                .sort("position", Sort.ASCENDING);
        List<FlashcardDO> copies = new ArrayList<>();
        for (FlashcardDO flashcard : flashcards) {
            FlashcardDO flashcardCopy = new FlashcardDO();
            flashcardCopy.setId(flashcard.getId());
            flashcardCopy.setTerm(flashcard.getTerm());
            flashcardCopy.setTermImageUrl(flashcard.getTermImageUrl());
            flashcardCopy.setDefinition(flashcard.getDefinition());
            flashcardCopy.setDefinitionImageUrl(flashcard.getDefinitionImageUrl());
            flashcardCopy.setLearned(flashcard.isLearned());
            flashcardCopy.setPosition(flashcard.getPosition());
            copies.add(flashcardCopy);
        }
        return copies;
    }

    public FlashcardSetDO getFlashcardSet(int setId) {
        return realm.where(FlashcardSetDO.class)
                .equalTo("id", setId)
                .findFirst();
    }

    public int getNumFlashcardSets() {
        return realm.where(FlashcardSetDO.class)
                .findAll()
                .size();
    }

    public void saveQuizletSet(QuizletFlashcardSet quizletFlashcardSet) {
        try {
            realm.beginTransaction();

            FlashcardSetDO set = new FlashcardSetDO();
            int newSetId = getNextSetId();
            set.setId(newSetId);
            set.setQuizletSetId(quizletFlashcardSet.getQuizletSetId());
            set.setName(quizletFlashcardSet.getTitle());

            RealmList<FlashcardDO> flashcards = new RealmList<>();
            int flashcardId = getNextFlashcardId();
            int position = 0;
            for (QuizletFlashcard quizletFlashcard : quizletFlashcardSet.getFlashcards()) {
                FlashcardDO flashcard = new FlashcardDO();
                flashcard.setId(flashcardId++);
                flashcard.setTerm(quizletFlashcard.getTerm());
                flashcard.setDefinition(quizletFlashcard.getDefinition());
                flashcard.setTermImageUrl(quizletFlashcard.getImageUrl());
                flashcard.setPosition(position++);
                flashcards.add(flashcard);
            }
            set.setFlashcards(flashcards);

            realm.copyToRealm(set);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    /**
     * Adds a flashcard set from nearby sharing to DB.
     */
    public int addExternalSetToDb(FlashcardSetDO flashcardSet) {
        try {
            realm.beginTransaction();

            FlashcardSetDO set = new FlashcardSetDO();
            int newSetId = getNextSetId();
            set.setId(newSetId);
            set.setQuizletSetId(flashcardSet.getQuizletSetId());
            set.setName(flashcardSet.getName());

            RealmList<FlashcardDO> flashcards = new RealmList<>();
            int flashcardId = getNextFlashcardId();
            for (FlashcardDO original : flashcardSet.getFlashcards()) {
                FlashcardDO flashcard = new FlashcardDO();
                flashcard.setId(flashcardId++);
                flashcard.setTerm(original.getTerm());
                flashcard.setDefinition(original.getDefinition());
                flashcard.setTermImageUrl(original.getTermImageUrl());
                flashcards.add(flashcard);
            }
            set.setFlashcards(flashcards);

            realm.copyToRealm(set);
            realm.commitTransaction();
            return newSetId;
        } catch (Exception e) {
            realm.cancelTransaction();
            return 0;
        }
    }

    public ArrayList<FlashcardSetPreview> restoreFlashcardSets(final List<FlashcardSetDO> flashcardSets) {
        final ArrayList<FlashcardSetPreview> previews = new ArrayList<>();
        Realm backgroundRealm = Realm.getDefaultInstance();
        backgroundRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Number setNumber = realm.where(FlashcardSetDO.class).findAll().max("id");
                int nextSetId = setNumber == null ? 1 : setNumber.intValue() + 1;

                Number flashcardNumber = realm.where(FlashcardDO.class).findAll().max("id");
                int nextCardId = flashcardNumber == null ? 1 : flashcardNumber.intValue() + 1;

                for (FlashcardSetDO flashcardSet : flashcardSets) {
                    FlashcardSetDO set = realm.createObject(FlashcardSetDO.class);
                    set.setId(nextSetId);
                    set.setQuizletSetId(flashcardSet.getQuizletSetId());
                    set.setName(flashcardSet.getName());

                    RealmList<FlashcardDO> flashcards = new RealmList<>();
                    for (FlashcardDO original : flashcardSet.getFlashcards()) {
                        FlashcardDO flashcard = realm.createObject(FlashcardDO.class);
                        flashcard.setId(nextCardId++);
                        flashcard.setTerm(original.getTerm());
                        flashcard.setDefinition(original.getDefinition());
                        flashcard.setTermImageUrl(original.getTermImageUrl());
                        flashcard.setPosition(original.getPosition());
                        flashcards.add(flashcard);
                    }
                    set.setFlashcards(flashcards);

                    nextSetId++;
                    previews.add(new FlashcardSetPreview(set));
                }
            }
        });
        return previews;
    }

    public boolean alreadyHasQuizletSet(long quizletSetId) {
        FlashcardSetDO set = realm
                .where(FlashcardSetDO.class)
                .equalTo("quizletSetId", quizletSetId)
                .findFirst();
        return set != null;
    }

    public int createFolder(String name) {
        try {
            realm.beginTransaction();
            FolderDO folderDO = new FolderDO();
            Number number = realm.where(FolderDO.class).findAll().max("id");
            int newFolderId = number == null ? 1 : number.intValue() + 1;
            folderDO.setId(newFolderId);
            folderDO.setName(name);
            realm.copyToRealm(folderDO);
            realm.commitTransaction();
            return newFolderId;
        } catch (Exception e) {
            realm.cancelTransaction();
        }
        return 0;
    }

    public List<Folder> getFolders(String searchTerm) {
        List<FolderDO> folderDOs;
        if (searchTerm.trim().isEmpty()) {
            folderDOs = realm
                    .where(FolderDO.class)
                    .findAll();
        } else {
            folderDOs = realm
                    .where(FolderDO.class)
                    .contains("name", searchTerm, Case.INSENSITIVE)
                    .findAll();
        }
        List<Folder> folders = new ArrayList<>();
        for (FolderDO folderDO : folderDOs) {
            folders.add(DBConverter.getFolderFromDO(folderDO));
        }
        return folders;
    }

    public void deleteFolder(Folder folder) {
        try {
            realm.beginTransaction();
            FolderDO folderToRemove = realm
                    .where(FolderDO.class)
                    .equalTo("id", folder.getId())
                    .findFirst();
            folderToRemove.deleteFromRealm();
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public Folder getFolder(int folderId) {
        FolderDO folderDO = realm
                .where(FolderDO.class)
                .equalTo("id", folderId)
                .findFirst();
        return DBConverter.getFolderFromDO(folderDO);
    }

    public List<FlashcardSetDO> getFlashcardSetsNotInFolder(int folderId) {
        FolderDO folderDO = realm.where(FolderDO.class)
                .equalTo("id", folderId)
                .findFirst();
        List<FlashcardSetDO> containedSets = folderDO.getFlashcardSets();
        HashSet<Integer> containedSetIds = new HashSet<>();
        for (FlashcardSetDO flashcardSet : containedSets) {
            containedSetIds.add(flashcardSet.getId());
        }
        List<FlashcardSetDO> allSets = realm.where(FlashcardSetDO.class).findAll();
        List<FlashcardSetDO> finalSets = new ArrayList<>();
        for (FlashcardSetDO flashcardSet : allSets) {
            if (!containedSetIds.contains(flashcardSet.getId())) {
                finalSets.add(flashcardSet);
            }
        }
        return finalSets;
    }

    public void addFlashcardSetsIntoFolder(int folderId, List<FlashcardSetDO> flashcardSets) {
        try {
            realm.beginTransaction();
            FolderDO folderDO = realm
                    .where(FolderDO.class)
                    .equalTo("id", folderId)
                    .findFirst();
            for (FlashcardSetDO flashcardSet : flashcardSets) {
                FlashcardSetDO setToAdd = realm
                        .where(FlashcardSetDO.class)
                        .equalTo("id", flashcardSet.getId())
                        .findFirst();
                folderDO.getFlashcardSets().add(setToAdd);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public List<FlashcardSetDO> getFlashcardSetsInFolder(int folderId) {
        return realm
                .where(FolderDO.class)
                .equalTo("id", folderId)
                .findFirst()
                .getFlashcardSets();
    }

    public void removeFlashcardSetFromFolder(int folderId, FlashcardSetDO setToRemove) {
        try {
            realm.beginTransaction();
            FolderDO folderDO = realm
                    .where(FolderDO.class)
                    .equalTo("id", folderId)
                    .findFirst();
            List<FlashcardSetDO> flashcardSets = folderDO.getFlashcardSets();
            for (int i = 0; i < flashcardSets.size(); i++) {
                if (flashcardSets.get(i).getId() == setToRemove.getId()) {
                    flashcardSets.remove(i);
                    break;
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void renameFolder(int folderId, String newName) {
        try {
            realm.beginTransaction();
            FolderDO folderDO = realm.where(FolderDO.class)
                    .equalTo("id", folderId)
                    .findFirst();
            folderDO.setName(newName);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public int getNumFolders() {
        return realm
                .where(FolderDO.class)
                .findAll()
                .size();
    }

    public boolean doesTermExist(int setId, String term) {
        RealmList<FlashcardDO> flashcards = realm.where(FlashcardSetDO.class)
                .equalTo("id", setId)
                .findFirst()
                .getFlashcards();
        return flashcards
                .where()
                .equalTo("term", term, Case.INSENSITIVE)
                .findFirst() != null;
    }

    public boolean doesDefinitionExist(int setId, String definition) {
        RealmList<FlashcardDO> flashcards = realm.where(FlashcardSetDO.class)
                .equalTo("id", setId)
                .findFirst()
                .getFlashcards();
        return flashcards
                .where()
                .equalTo("definition", definition, Case.INSENSITIVE)
                .findFirst() != null;
    }

    public void moveFlashcards(int receivingSetId, int sendingSetId, Set<Integer> flashcardIds) {
        RealmList<FlashcardDO> flashcardsToSend = realm.where(FlashcardSetDO.class)
                .equalTo("id", sendingSetId)
                .findFirst()
                .getFlashcards();
        try {
            realm.beginTransaction();
            FlashcardSetDO receivingSet = realm
                    .where(FlashcardSetDO.class)
                    .equalTo("id", receivingSetId)
                    .findFirst();
            for (int i = 0; i < flashcardsToSend.size(); i++) {
                if (flashcardIds.contains(flashcardsToSend.get(i).getId())) {
                    FlashcardDO flashcardToAdd = flashcardsToSend.get(i);
                    flashcardsToSend.remove(i);
                    receivingSet.getFlashcards().add(flashcardToAdd);
                    i--;
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void copyFlashcards(int receivingSetId, int sendingSetId, Set<Integer> flashcardIds) {
        RealmList<FlashcardDO> flashcardsToSend = realm.where(FlashcardSetDO.class)
                .equalTo("id", sendingSetId)
                .findFirst()
                .getFlashcards();
        try {
            realm.beginTransaction();
            FlashcardSetDO receivingSet = realm
                    .where(FlashcardSetDO.class)
                    .equalTo("id", receivingSetId)
                    .findFirst();
            int nextCardId = getNextFlashcardId();
            for (int i = 0; i < flashcardsToSend.size(); i++) {
                if (flashcardIds.contains(flashcardsToSend.get(i).getId())) {
                    FlashcardDO originalCard = flashcardsToSend.get(i);
                    FlashcardDO newFlashcard = new FlashcardDO();
                    newFlashcard.setId(nextCardId);
                    newFlashcard.setTerm(originalCard.getTerm());
                    newFlashcard.setDefinition(originalCard.getDefinition());
                    newFlashcard.setTermImageUrl(originalCard.getTermImageUrl());
                    newFlashcard.setDefinitionImageUrl(originalCard.getDefinitionImageUrl());
                    receivingSet.getFlashcards().add(newFlashcard);
                    nextCardId++;
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void setLearnedStatus(int flashcardId, boolean learned) {
        try {
            realm.beginTransaction();
            FlashcardDO flashcardDO = realm.where(FlashcardDO.class)
                    .equalTo("id", flashcardId)
                    .findFirst();
            flashcardDO.setLearned(learned);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void updateSetLanguages(int setId, @Language int termsLanguage, @Language int definitionsLanguage) {
        try {
            realm.beginTransaction();
            FlashcardSetDO flashcardSetDO = realm.where(FlashcardSetDO.class)
                    .equalTo("id", setId)
                    .findFirst();
            flashcardSetDO.setTermsLanguage(termsLanguage);
            flashcardSetDO.setDefinitionsLanguage(definitionsLanguage);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void setFlashcardPositions(List<FlashcardDO> cardsInNewOrder) {
        try {
            realm.beginTransaction();
            for (int i = 0; i < cardsInNewOrder.size(); i++) {
                FlashcardDO flashcardDO = realm.where(FlashcardDO.class)
                        .equalTo("id", cardsInNewOrder.get(i).getId())
                        .findFirst();
                flashcardDO.setPosition(i);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }

    public void addFlashcardSet(String setName, List<Flashcard> flashcards) {
        int setId = createFlashcardSet(setName);
        for (Flashcard flashcard : flashcards) {
            addFlashcard(setId, flashcard.getTerm(), flashcard.getDefinition());
        }
    }

    public void setAllFlashcardLearnedStatuses(int setId, boolean learned) {
        try {
            realm.beginTransaction();
            FlashcardSetDO flashcardSetDO = realm
                    .where(FlashcardSetDO.class)
                    .equalTo("id", setId)
                    .findFirst();
            List<FlashcardDO> flashcards = flashcardSetDO.getFlashcards();
            for (FlashcardDO flashcardDO : flashcards) {
                flashcardDO.setLearned(learned);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
        }
    }
}
