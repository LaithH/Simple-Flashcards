package com.randomappsinc.simpleflashcards.ocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.dialogs.ConfirmQuitDialog;
import com.randomappsinc.simpleflashcards.utils.PermissionUtils;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OcrActivity extends StandardActivity
        implements PhotoTakerManager.Listener, TextRecognitionManager.Listener, ConfirmQuitDialog.Listener {

    // Request codes
    private static final int CAMERA_CODE = 1;

    @BindView(R.id.set_name_input) EditText setNameInput;
    @BindView(R.id.flashcards) RecyclerView flashcardsList;
    @BindView(R.id.add_flashcard) FloatingActionButton addFlashcard;

    private PhotoTakerManager photoTakerManager;
    private TextRecognitionManager textRecognitionManager;
    private MaterialDialog progressDialog;
    private ConfirmQuitDialog confirmQuitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_creator_page);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new MaterialDialog.Builder(this)
                .theme(themeManager.getDarkModeEnabled(this) ? Theme.DARK : Theme.LIGHT)
                .content(R.string.processing_image)
                .progress(true, 0)
                .cancelable(false)
                .build();

        addFlashcard.setImageDrawable(
                new IconDrawable(this, IoniconsIcons.ion_android_add)
                        .colorRes(R.color.white));
        photoTakerManager = new PhotoTakerManager(this);
        textRecognitionManager = new TextRecognitionManager(this, this);
        confirmQuitDialog = new ConfirmQuitDialog(this, this, R.string.confirm_ocr_quit_body);
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
                progressDialog.setContent(R.string.processing_image);
                progressDialog.show();
                photoTakerManager.processTakenPhoto(this);
            } else if (resultCode == RESULT_CANCELED) {
                photoTakerManager.deleteLastTakenPhoto();
            }
        }
    }

    @Override
    public void onTakePhotoFailure() {
        progressDialog.dismiss();
        UIUtils.showLongToast(R.string.take_photo_with_camera_failed, this);
    }

    @Override
    public void onTakePhotoSuccess(Bitmap bitmap) {
        runOnUiThread(() -> progressDialog.setContent(R.string.recognizing_text));
        textRecognitionManager.analyzeImage(bitmap);
    }

    @Override
    public void onTextBlocksRecognized(List<String> textBlocks) {
        runOnUiThread(() -> {
            progressDialog.dismiss();
            if (textBlocks.isEmpty()) {
                UIUtils.showLongToast(R.string.no_ocr_text_found, this);
                return;
            }
            StringBuilder everything = new StringBuilder();
            for (String text : textBlocks) {
                if (everything.length() > 0) {
                    everything.append("\n\n");
                }
                everything.append(text);
            }
            UIUtils.showLongToast(everything.toString(), this);
        });
    }

    @Override
    public void onTextRecognitionFailed() {
        runOnUiThread(() -> {
            progressDialog.dismiss();
            UIUtils.showLongToast(R.string.ocr_error, this);
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

    @OnClick(R.id.save)
    public void saveFlashcardSet() {
        String setName = setNameInput.getText().toString().trim();
        if (setName.isEmpty()) {
            UIUtils.showLongToast(R.string.empty_set_name, this);
            return;
        }
    }

    @Override
    public void onQuitConfirmed() {
        finish();
    }

    @Override
    public void onBackPressed() {
        confirmQuitDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        photoTakerManager.deleteLastTakenPhoto();
        textRecognitionManager.cleanUp();
        confirmQuitDialog.cleanUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            confirmQuitDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
