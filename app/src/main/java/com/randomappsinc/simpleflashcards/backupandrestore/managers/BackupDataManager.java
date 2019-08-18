package com.randomappsinc.simpleflashcards.backupandrestore.managers;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.utils.JSONUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class BackupDataManager {

    public static final String BACKUP_FILE_NAME = "simple-flashcards-plus-backup.txt";

    public interface Listener {
        void onBackupComplete();

        void onBackupFailed();
    }

    private static BackupDataManager instance;

    public static BackupDataManager get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized BackupDataManager getSync() {
        if (instance == null) {
            instance = new BackupDataManager();
        }
        return instance;
    }

    private Handler backgroundHandler;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    protected DatabaseManager databaseManager = DatabaseManager.get();
    @Nullable protected Listener listener;

    private BackupDataManager() {
        HandlerThread handlerThread = new HandlerThread("Backup Data");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    public void setBackupLocation(String folderPath, Context context) {
        PreferencesManager preferencesManager = new PreferencesManager(context);

        // Clear out backup URI (should never happen, but let's be safe)
        preferencesManager.setBackupUri(null);

        preferencesManager.setBackupFilePath(folderPath + "/" + BACKUP_FILE_NAME);
        backupData(context, true);
    }

    @Nullable
    public String getBackupPath(Context context) {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String backupFilePath = preferencesManager.getBackupFilePath();
        if (backupFilePath != null) {
            return backupFilePath;
        }
        return preferencesManager.getBackupUri();
    }

    @Nullable
    public Uri getBackupUriForExporting(Context context) {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String backupFilePath = preferencesManager.getBackupFilePath();
        if (backupFilePath != null) {
            return FileProvider.getUriForFile(
                    context,
                    Constants.FILE_PROVIDER_AUTHORITY,
                    new File(backupFilePath));
        }
        String backupUri = preferencesManager.getBackupUri();
        if (backupUri != null) {
            return Uri.parse(backupUri);
        }
        return null;
    }

    public void backupData(final Context context, final boolean userTriggered) {
        final PreferencesManager preferencesManager = new PreferencesManager(context);

        // Try the File IO strategy (should only apply on pre-KitKat devices)
        final String backupFolderPath = preferencesManager.getBackupFilePath();
        if (backupFolderPath != null) {
            backgroundHandler.post(() -> {
                File file = new File(backupFolderPath);
                try {
                    FileOutputStream stream = new FileOutputStream(file);
                    List<FlashcardSetDO> flashcardSets = databaseManager.getAllFlashcardSetsOnAnyThread();
                    stream.write(JSONUtils.serializeFlashcardSets(flashcardSets).getBytes());
                    stream.close();
                    preferencesManager.updateLastBackupTime();
                    if (userTriggered) {
                        alertListenerOfBackupComplete();
                    }
                } catch (Exception exception) {
                    if (userTriggered) {
                        alertListenerOfBackupFail();
                    }
                }
            });
        }
        // Try the Storage Access Framework URI strategy for KitKat+
        else if (preferencesManager.getBackupUri() != null){
            final String backupUri = preferencesManager.getBackupUri();
            backgroundHandler.post(() -> {
                try {
                    ParcelFileDescriptor fileDescriptor = context.getContentResolver().
                            openFileDescriptor(Uri.parse(backupUri), "w");
                    if (fileDescriptor == null) {
                        if (userTriggered) {
                            alertListenerOfBackupFail();
                        }
                        return;
                    }
                    FileOutputStream fileOutputStream =
                            new FileOutputStream(fileDescriptor.getFileDescriptor());
                    List<FlashcardSetDO> flashcardSets = databaseManager.getAllFlashcardSetsOnAnyThread();
                    fileOutputStream.write(JSONUtils.serializeFlashcardSets(flashcardSets).getBytes());
                    fileOutputStream.close();
                    fileDescriptor.close();
                    preferencesManager.updateLastBackupTime();
                    if (userTriggered) {
                        alertListenerOfBackupComplete();
                    }
                } catch (Exception exception) {
                    if (userTriggered) {
                        alertListenerOfBackupFail();
                    }
                }
            });
        }
        // If we have no backup paths to write to and we called this method, something's wrong
        else {
            if (userTriggered) {
                alertListenerOfBackupFail();
            }
        }
    }

    protected void alertListenerOfBackupComplete() {
        if (listener != null) {
            uiHandler.post(() -> listener.onBackupComplete());
        }
    }

    protected void alertListenerOfBackupFail() {
        if (listener != null) {
            uiHandler.post(() -> listener.onBackupFailed());
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }
}
