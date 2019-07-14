package com.randomappsinc.simpleflashcards.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.common.constants.Constants;

public class PreferencesManager {

    private static final int DEFAULT_BROWSE_TEXT_SIZE_SP = 24;

    private static final String NUM_OPENS_KEY = "numOpens";
    private static final String NEARBY_NAME = "nearbyName";
    private static final String ENABLE_SHAKE = "enableShake";

    // Pre-Kitkat (19), we work with file paths
    private static final String BACKUP_FILE_PATH = "backupFilePath";

    // On KitKat+, we work with URIs (Storage Access Framework)
    private static final String BACKUP_URI = "backupUri";

    private static final String LAST_BACKUP_TIME = "lastBackupTime";

    private static final String RATING_DIALOG_SEEN = "ratingDialogSeen";
    private static final String SHARING_DIALOG_SEEN = "sharingDialogSeen";
    private static final String BACKUP_DATA_DIALOG_SEEN = "backupDataDialogSeen";

    private static final int NUM_APP_OPENS_BEFORE_TEACHING_DARK_MODE = 3;
    private static final int NUM_APP_OPENS_BEFORE_ASKING_FOR_RATING = 5;
    private static final int NUM_APP_OPENS_BEFORE_ASKING_FOR_SHARE = 10;

    private static final String DARK_MODE_ENABLED = "darkModeEnabled";
    private static final String HAS_SEEN_DARK_MODE_DIALOG = "hasSeenDarkModeDialog";

    private static final String BROWSE_DO_NOT_SHOW_LEARNED = "browseDoNotShowLearned";
    private static final String BROWSE_TEXT_SIZE = "browseTextSize";
    private static final String BROWSE_TEXT_COLOR = "browseTextColor";

    private static final String SHOULD_TEACH_FLASHCARDS_REORDER = "shouldTeachFlashcardsReorder";

    private SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void logAppOpen() {
        int currentOpens = prefs.getInt(NUM_OPENS_KEY, 0);
        prefs.edit().putInt(NUM_OPENS_KEY, ++currentOpens).apply();
    }

    public boolean shouldAskForRating() {
        int currentOpens = prefs.getInt(NUM_OPENS_KEY, 0);
        boolean ratingDialogSeen = prefs.getBoolean(RATING_DIALOG_SEEN, false);
        return currentOpens >= NUM_APP_OPENS_BEFORE_ASKING_FOR_RATING && !ratingDialogSeen;
    }

    public void rememberRatingDialogSeen() {
        prefs.edit().putBoolean(RATING_DIALOG_SEEN, true).apply();
    }

    public boolean shouldAskForShare() {
        int currentOpens = prefs.getInt(NUM_OPENS_KEY, 0);
        boolean sharingDialogSeen = prefs.getBoolean(SHARING_DIALOG_SEEN, false);
        return currentOpens >= NUM_APP_OPENS_BEFORE_ASKING_FOR_SHARE && !sharingDialogSeen;
    }

    public void rememberSharingDialogSeen() {
        prefs.edit().putBoolean(SHARING_DIALOG_SEEN, true).apply();
    }

    public boolean hasSeenBackupDataDialog() {
        return prefs.getBoolean(BACKUP_DATA_DIALOG_SEEN, false);
    }

    public void rememberBackupDataDialogSeen() {
        prefs.edit().putBoolean(BACKUP_DATA_DIALOG_SEEN, true).apply();
    }

    public String getNearbyName() {
        return prefs.getString(NEARBY_NAME, "");
    }

    public void setNearbyName(String newName) {
        prefs.edit().putString(NEARBY_NAME, newName).apply();
    }

    public boolean isShakeEnabled() {
        return prefs.getBoolean(ENABLE_SHAKE, true);
    }

    public void setShakeEnabled(boolean enableShake) {
        prefs.edit().putBoolean(ENABLE_SHAKE, enableShake).apply();
    }

    @Nullable
    public String getBackupFilePath() {
        return prefs.getString(BACKUP_FILE_PATH, null);
    }

    public void setBackupFilePath(@Nullable String backupFilePath) {
        prefs.edit().putString(BACKUP_FILE_PATH, backupFilePath).apply();
    }

    @Nullable
    public String getBackupUri() {
        return prefs.getString(BACKUP_URI, null);
    }

    public void setBackupUri(@Nullable String uriString) {
        prefs.edit().putString(BACKUP_URI, uriString).apply();
    }

    public long getLastBackupTime() {
        return prefs.getLong(LAST_BACKUP_TIME, 0);
    }

    public void updateLastBackupTime() {
        prefs.edit().putLong(LAST_BACKUP_TIME, System.currentTimeMillis()).apply();
    }

    public boolean getDarkModeEnabled() {
        return prefs.getBoolean(DARK_MODE_ENABLED, false);
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        prefs.edit().putBoolean(DARK_MODE_ENABLED, darkModeEnabled).apply();
    }

    public boolean shouldTeachAboutDarkMode() {
        int currentOpens = prefs.getInt(NUM_OPENS_KEY, 0);
        boolean darkModeDisabled = !getDarkModeEnabled();
        return currentOpens >= NUM_APP_OPENS_BEFORE_TEACHING_DARK_MODE
                && darkModeDisabled
                && !prefs.getBoolean(HAS_SEEN_DARK_MODE_DIALOG, false);
    }

    public void rememberDarkModeDialogSeen() {
        prefs.edit().putBoolean(HAS_SEEN_DARK_MODE_DIALOG, true).apply();
    }

    public boolean getBrowseDoNotShowLearned() {
        return prefs.getBoolean(BROWSE_DO_NOT_SHOW_LEARNED, false);
    }

    public void setBrowseDoNotShowLearned(boolean doNotShowLearned) {
        prefs.edit().putBoolean(BROWSE_DO_NOT_SHOW_LEARNED, doNotShowLearned).apply();
    }

    public int getBrowseTextSize() {
        return prefs.getInt(BROWSE_TEXT_SIZE, DEFAULT_BROWSE_TEXT_SIZE_SP);
    }

    public void setBrowseTextSize(int newSizeSp) {
        prefs.edit().putInt(BROWSE_TEXT_SIZE, newSizeSp).apply();
    }

    public int getBrowseTextColor() {
        return prefs.getInt(BROWSE_TEXT_COLOR, Constants.UNSET_COLOR);
    }

    public void setBrowseTextColor(int newColor) {
        prefs.edit().putInt(BROWSE_TEXT_COLOR, newColor).apply();
    }

    public boolean shouldTeachFlashcardsReorder() {
        boolean shouldTeach = prefs.getBoolean(SHOULD_TEACH_FLASHCARDS_REORDER, true);
        prefs.edit().putBoolean(SHOULD_TEACH_FLASHCARDS_REORDER, false).apply();
        return shouldTeach;
    }
}
