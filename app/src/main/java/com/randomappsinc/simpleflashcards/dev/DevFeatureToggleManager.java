package com.randomappsinc.simpleflashcards.dev;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages everything when it comes to feature toggles (fetching, reading, writing, etc).
 * Lovingly clumped into 1 big class because why not.
 */
public class DevFeatureToggleManager {

    // Prefix all writes with this prefix so we don't overwrite actual things in shared preferences
    private static final String DEV_PREFIX = "dev_";

    private static DevFeatureToggleManager instance;

    public static DevFeatureToggleManager get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized DevFeatureToggleManager getSync() {
        if (instance == null) {
            instance = new DevFeatureToggleManager();
        }
        return instance;
    }

    private List<String> featureToggles = new ArrayList<>();

    private DevFeatureToggleManager() {
        featureToggles.add(DevFeatureToggles.RANDOM_TEST_FEATURE);
    }

    public List<String> getAllFeatureToggles() {
        return Collections.unmodifiableList(featureToggles);
    }

    public void setFeatureEnabled(Context context, String featureToggleName, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(DEV_PREFIX + featureToggleName, enabled).apply();
    }

    public boolean isFeatureEnabled(Context context, String featureToggleName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(DEV_PREFIX + featureToggleName, false);
    }
}
