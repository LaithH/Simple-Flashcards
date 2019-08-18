package com.randomappsinc.simpleflashcards.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    @Nullable
    private static Bitmap getBitmapFromFileProviderUri(Context context, Uri takenPhotoUri) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            InputStream input = contentResolver.openInputStream(takenPhotoUri);
            return BitmapFactory.decodeStream(input, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap rotateImageIfRequired(Context context, File photoFile, Uri takenPhotoUri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(takenPhotoUri);
        ExifInterface exifInterface = new ExifInterface(input);

        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Bitmap rotatedBitmap;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap =  rotateImage(context, takenPhotoUri,90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(context, takenPhotoUri,180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(context, takenPhotoUri,270);
                break;
            default:
                return getBitmapFromFileProviderUri(context, takenPhotoUri);
        }
        if (rotatedBitmap != null) {
            FileOutputStream out = new FileOutputStream(photoFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();
        }

        return rotatedBitmap;
    }

    private static Bitmap rotateImage(Context context, Uri takenPhotoUri, int degree) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = getBitmapFromFileProviderUri(context, takenPhotoUri);
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotatedImg;
    }
}
