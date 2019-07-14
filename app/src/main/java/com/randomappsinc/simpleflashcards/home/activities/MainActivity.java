package com.randomappsinc.simpleflashcards.home.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.backupandrestore.managers.BackupDataManager;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.home.fragments.HomepageFragmentController;
import com.randomappsinc.simpleflashcards.home.views.BottomNavigationView;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.utils.DialogUtil;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends StandardActivity implements BottomNavigationView.Listener {

    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigation;
    @BindView(R.id.bottom_sheet) View bottomSheet;

    private BottomSheetBehavior bottomSheetBehavior;
    private HomepageFragmentController navigationController;
    protected BackupDataManager backupDataManager = BackupDataManager.get();
    private DatabaseManager databaseManager = DatabaseManager.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomNavigation.maybeResetAddButton();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
        bottomNavigation.setListener(this);
        navigationController = new HomepageFragmentController(getSupportFragmentManager(), R.id.container);
        navigationController.loadHomeInitially();

        PreferencesManager preferencesManager = new PreferencesManager(this);
        preferencesManager.logAppOpen();
        DialogUtil.showHomepageDialog(this);

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

    public void loadQuizletSetSearch() {
        bottomNavigation.onSearchClicked();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        UIUtils.loadMenuIcon(menu, R.id.sort_flashcard_sets, FontAwesomeIcons.fa_sort_alpha_asc, this);
        UIUtils.loadMenuIcon(menu, R.id.filter, IoniconsIcons.ion_funnel, this);
        return true;
    }
}
