package com.randomappsinc.simpleflashcards.ocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.utils.ImageUtils;
import com.randomappsinc.simpleflashcards.utils.PermissionUtils;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.BitSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.internal.OsResults;

public class OcrActivity extends StandardActivity implements PhotoTakerManager.Listener {

    // Request codes
    private static final int CAMERA_CODE = 1;

    @BindView(R.id.flashcards) RecyclerView flashcardsList;
    @BindView(R.id.add_flashcard) FloatingActionButton addFlashcard;
    @BindView(R.id.bitmap_view) ImageView imageView;

    private PhotoTakerManager photoTakerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_creator_page);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addFlashcard.setImageDrawable(
                new IconDrawable(this, IoniconsIcons.ion_android_add)
                        .colorRes(R.color.white));
        photoTakerManager = new PhotoTakerManager(this);
        maybeStartCameraPage();
    }

    @OnClick(R.id.add_flashcard)
    public void addFlashcard() {
        maybeStartCameraPage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == CAMERA_CODE) {
            if (resultCode == RESULT_OK) {
                photoTakerManager.processTakenPhoto(this);
            } else if (resultCode == RESULT_CANCELED) {
                photoTakerManager.deleteLastTakenPhoto();
            }
        }
    }

    @Override
    public void onTakePhotoFailure() {
        UIUtils.showLongToast(R.string.take_photo_with_camera_failed, this);
    }

    @Override
    public void onTakePhotoSuccess(Bitmap bitmap) {
        runOnUiThread(() -> {
            imageView.setImageBitmap(bitmap);
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();

            SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

            Log.d("poop", textBlocks.size() + "");
            for(int i = 0; i < textBlocks.size(); i++) {
                int key = textBlocks.keyAt(i);
                // get the object by the key.
                TextBlock textBlock = textBlocks.get(key);
                Log.d("poop", textBlock.getValue() + "");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode != CAMERA_CODE
                || grantResults.length <= 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        // Camera permission granted
        maybeStartCameraPage();
    }

    private void maybeStartCameraPage() {
        if (PermissionUtils.isPermissionGranted(Manifest.permission.CAMERA, this)) {
            Intent takePhotoIntent = photoTakerManager.getPhotoTakingIntent(this);
            if (takePhotoIntent == null) {
                UIUtils.showLongToast(R.string.take_photo_with_camera_failed, this);
            } else {
                UIUtils.showLongToast(R.string.ocr_image_instructions, this);
                startActivityForResult(takePhotoIntent, CAMERA_CODE);
            }
        } else {
            PermissionUtils.requestPermission(this, Manifest.permission.CAMERA, CAMERA_CODE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        photoTakerManager.deleteLastTakenPhoto();
    }
}
