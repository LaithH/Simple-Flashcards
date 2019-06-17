package com.randomappsinc.simpleflashcards.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class FileUtils {

    @Nullable
    public static File writeFlashcardSetToFile(FlashcardSetDO flashcardSet, Context context) {
        String filename = String.valueOf(flashcardSet.getId());
        File file = new File(context.getFilesDir(), filename);
        String fileContents = JSONUtils.serializeFlashcardSet(flashcardSet);

        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFileContents(File file) {
        Scanner scanner = null;
        String contents = "";
        try {
            scanner = new Scanner(file);
            contents = scanner.useDelimiter("\\A").next();
        }
        catch (FileNotFoundException ignored) {}
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return contents;
    }

    @Nullable
    public static File createImageFile(Context context) {
        File imageFile;
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "SIMPLE_FLASHCARDS_PLUS_" + timeStamp + "_";
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir);
        } catch (IOException exception) {
            return null;
        }
        return imageFile;
    }

    public static void maybeDeleteFileWithUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return;
        }

        String filePath = uri.substring(uri.lastIndexOf('/'));
        String completePath = Environment.getExternalStorageDirectory().getPath()
                + "/Android/data/com.randomappsinc.simpleflashcards/files/Pictures"
                + filePath;
        File imageFile = new File(completePath);
        if (imageFile.exists()) {
            imageFile.delete();
        }
    }

    public static boolean copyFromUriIntoFile(ContentResolver contentResolver, Uri sourceUri, Uri targetUri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = contentResolver.openInputStream(sourceUri);
            outputStream = contentResolver.openOutputStream(targetUri);
            if (inputStream == null || outputStream == null) {
                return false;
            }
            byte[] buf = new byte[1024];
            if (inputStream.read(buf) <= 0) {
                return false;
            }
            do {
                outputStream.write(buf);
            } while (inputStream.read(buf) != -1);
        } catch (IOException ignored) {
            return false;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException ignored) {}
        }
        return true;
    }

    @Nullable
    public static File createCsvFileForSet(Context context, FlashcardSetDO flashcardSetDO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return null;
        }
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        return new File(storageDir, flashcardSetDO.getName() + ".csv");
    }
}
