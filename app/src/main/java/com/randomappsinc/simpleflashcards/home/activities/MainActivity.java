package com.randomappsinc.simpleflashcards.home.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.backupandrestore.activities.BackupAndRestoreActivity;
import com.randomappsinc.simpleflashcards.backupandrestore.managers.BackupDataManager;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.csvimport.CsvImportActivity;
import com.randomappsinc.simpleflashcards.dev.DevFeatureToggleManager;
import com.randomappsinc.simpleflashcards.dev.DevFeatureToggles;
import com.randomappsinc.simpleflashcards.editflashcards.activities.EditFlashcardsActivity;
import com.randomappsinc.simpleflashcards.home.dialogs.CreateFlashcardSetDialog;
import com.randomappsinc.simpleflashcards.home.fragments.HomepageFragmentController;
import com.randomappsinc.simpleflashcards.home.views.BottomNavigationView;
import com.randomappsinc.simpleflashcards.nearbysharing.activities.NearbySharingActivity;
import com.randomappsinc.simpleflashcards.ocr.OcrActivity;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.utils.DialogUtil;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends StandardActivity
        implements BottomNavigationView.Listener, CreateFlashcardSetDialog.Listener {

    private static final int CSV_IMPORT_REQUEST_CODE = 2;

    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigation;
    @BindView(R.id.bottom_sheet) View bottomSheet;
    @BindView(R.id.create_with_ocr_divider) View createWithOcrDivider;
    @BindView(R.id.sheet_create_with_ocr) View createWithOcrOption;

    protected BottomSheetBehavior bottomSheetBehavior;
    private HomepageFragmentController navigationController;
    protected BackupDataManager backupDataManager = BackupDataManager.get();
    private DatabaseManager databaseManager = DatabaseManager.get();
    private CreateFlashcardSetDialog createFlashcardSetDialog;
    private DevFeatureToggleManager featureToggleManager = DevFeatureToggleManager.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PreferencesManager preferencesManager = new PreferencesManager(this);
        preferencesManager.logAppOpen();
        DialogUtil.showHomepageDialog(this);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Set state to HIDDEN on slideOffset being -1 (fully hidden),
                // because if you expand/collapse it super fast, the state machine is broken
                if (slideOffset == -1) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                bottomNavigation.onAddSheetSlideOffset(slideOffset);
            }
        });
        bottomNavigation.setListener(this);
        navigationController = new HomepageFragmentController(getSupportFragmentManager(), R.id.container);
        navigationController.loadHomeInitially();

        createFlashcardSetDialog = new CreateFlashcardSetDialog(this, this);

        databaseManager.setListener(databaseListener);
    }

    private final DatabaseManager.Listener databaseListener
            = () -> backupDataManager.backupData(getApplicationContext(), false);

    @Override
    public void onNavItemSelected(int viewId) {
        UIUtils.closeKeyboard(this);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        navigationController.onNavItemSelected(viewId);

        switch (viewId) {
            case R.id.home:
                setTitle(R.string.app_name);
                break;
            case R.id.search:
                setTitle(R.string.download_flashcard_sets_title);
                break;
            case R.id.folders:
                setTitle(R.string.folders);
                break;
            case R.id.settings:
                setTitle(R.string.settings);
                break;
        }
    }

    @Override
    public void onAddOptionsExpanded() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onAddOptionsContracted() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @OnClick(R.id.sheet_download_flashcards)
    public void downloadSets() {
        hideBottomSheet();
        bottomNavigation.onSearchClicked();
    }

    @OnClick(R.id.sheet_create_with_ocr)
    public void createWithOcr() {
        hideBottomSheet();
        startActivity(new Intent(this, OcrActivity.class));
    }

    @OnClick(R.id.sheet_create_set)
    public void createSet() {
        hideBottomSheet();
        createFlashcardSetDialog.show();
    }

    @OnClick(R.id.sheet_import_from_csv)
    public void importFromCsv() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hideBottomSheet();
            UIUtils.showLongToast(R.string.csv_format_instructions, this);
            Intent csvIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            csvIntent.addCategory(Intent.CATEGORY_OPENABLE);
            csvIntent.setType("*/*");
            csvIntent.putExtra(Intent.EXTRA_MIME_TYPES, Constants.CSV_MIME_TYPES);
            startActivityForResult(csvIntent, CSV_IMPORT_REQUEST_CODE);
        }
    }

    @OnClick(R.id.sheet_share_nearby)
    public void shareWithNearby() {
        hideBottomSheet();
        startActivity(new Intent(this, NearbySharingActivity.class));
    }

    @OnClick(R.id.sheet_restore_from_backup)
    public void restoreFromBackup() {
        hideBottomSheet();
        Intent intent = new Intent(this, BackupAndRestoreActivity.class)
                .putExtra(Constants.GO_TO_RESTORE_IMMEDIATELY_KEY, true);
        startActivity(intent);
    }

    public void hideBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onFlashcardSetCreated(String newSetName) {
        UIUtils.showShortToast(R.string.flashcard_set_created, this);
        int newSetId = databaseManager.createFlashcardSet(newSetName);
        Intent intent = new Intent(this, EditFlashcardsActivity.class);
        intent.putExtra(Constants.FLASHCARD_SET_ID_KEY, newSetId);
        startActivity(intent);
    }

    public void loadQuizletSetSearch() {
        bottomNavigation.onSearchClicked();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CSV_IMPORT_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && resultCode == Activity.RESULT_OK
                    && data != null && data.getData() != null) {
                Uri uri = data.getData();

                // Persist ability to read from this file
                int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

                String uriString = uri.toString();
                Intent intent = new Intent(this, CsvImportActivity.class);
                intent.putExtra(Constants.URI_KEY, uriString);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean createWithOcrEnabled = featureToggleManager.isFeatureEnabled(
                this, DevFeatureToggles.CREATE_WITH_OCR);
        createWithOcrDivider.setVisibility(createWithOcrEnabled ? View.VISIBLE : View.GONE);
        createWithOcrOption.setVisibility(createWithOcrEnabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        createFlashcardSetDialog.cleanUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        UIUtils.loadMenuIcon(menu, R.id.sort_flashcard_sets, FontAwesomeIcons.fa_sort_alpha_asc, this);
        UIUtils.loadMenuIcon(menu, R.id.filter, IoniconsIcons.ion_funnel, this);
        return true;
    }
}
