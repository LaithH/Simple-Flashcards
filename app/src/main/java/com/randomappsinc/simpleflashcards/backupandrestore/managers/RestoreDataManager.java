package com.randomappsinc.simpleflashcards.backupandrestore.managers;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.common.models.FlashcardSetPreview;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSet;
import com.randomappsinc.simpleflashcards.utils.JSONUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RestoreDataManager {

    public interface Listener {
        void onDataRestorationStarted();

        void onDataRestorationComplete(ArrayList<FlashcardSetPreview> addedSetIds);

        void onDataRestorationFailed();

        void onFileNotFound();
    }

    private static RestoreDataManager instance;

    public static RestoreDataManager get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized RestoreDataManager getSync() {
        if (instance == null) {
            instance = new RestoreDataManager();
        }
        return instance;
    }

    @Nullable protected Listener listener;
    private Handler backgroundHandler;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    protected DatabaseManager databaseManager = DatabaseManager.get();

    private RestoreDataManager() {
        HandlerThread handlerThread = new HandlerThread("Restore Data");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void restoreDataFromFolderPath(String folderPath) {
        if (listener != null) {
            listener.onDataRestorationStarted();
        }

        final File backupFile = new File(folderPath + "/" + BackupDataManager.BACKUP_FILE_NAME);
        if (backupFile.exists()) {
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<FlashcardSet> flashcardSets = JSONUtils.getSetsForDataRestoration(backupFile);
                    ArrayList<FlashcardSetPreview> previews = databaseManager.restoreFlashcardSets(flashcardSets);
                    alertOfDataRestorationComplete(previews);
                }
            });
        } else {
            if (listener != null) {
                listener.onFileNotFound();
            }
        }
    }

    public void restoreDataFromUri(final Uri uri, final Context context) {
        if (listener != null) {
            listener.onDataRestorationStarted();
        }

        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream == null) {
                        alertOfFileNotFound();
                        return;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                    String setsJson = stringBuilder.toString();
                    List<FlashcardSet> flashcardSets = JSONUtils.deserializeSets(setsJson);
                    ArrayList<FlashcardSetPreview> previews = databaseManager.restoreFlashcardSets(flashcardSets);
                    alertOfDataRestorationComplete(previews);
                } catch (IOException exception) {
                    alertOfDataRestorationFailed();
                }
            }
        });
    }

    protected void alertOfFileNotFound() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onFileNotFound();
                }
            }
        });
    }

    protected void alertOfDataRestorationFailed() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDataRestorationFailed();
                }
            }
        });
    }

    public void alertOfDataRestorationComplete(final ArrayList<FlashcardSetPreview> previews) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDataRestorationComplete(previews);
                }
            }
        });
    }
}
