package com.randomappsinc.simpleflashcards.home.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomNavigationView extends FrameLayout implements ThemeManager.Listener {

    private static final float ADD_BUTTON_ROTATION_ANGLE = 45.0f;

    public interface Listener {
        void onNavItemSelected(@IdRes int viewId);

        void onAddOptionsExpanded();

        void onAddOptionsContracted();
    }

    @BindView(R.id.home) TextView homeButton;
    @BindView(R.id.search) TextView searchButton;
    @BindView(R.id.add) TextView addButton;
    @BindView(R.id.folders) TextView folderButton;
    @BindView(R.id.settings) TextView settingsButton;

    @BindColor(R.color.dark_gray) int darkGray;
    @BindColor(R.color.app_blue) int blue;
    @BindColor(R.color.half_white) int halfWhite;
    @BindColor(R.color.white) int white;

    private int selectedColor;
    private int nonSelectedColor;

    private Listener listener;
    private TextView currentlySelected;
    private ThemeManager themeManager = ThemeManager.get();
    private boolean isAddButtonExpanded = false;

    public BottomNavigationView(Context context) {
        this(context, null, 0);
    }

    public BottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(getContext(), R.layout.bottom_navigation, this);
        ButterKnife.bind(this);
        setColors();
        currentlySelected = homeButton;
        homeButton.setTextColor(selectedColor);
        searchButton.setTextColor(nonSelectedColor);
        folderButton.setTextColor(nonSelectedColor);
        settingsButton.setTextColor(nonSelectedColor);
    }

    private void setColors() {
        selectedColor = themeManager.getDarkModeEnabled(getContext()) ? white : blue;
        nonSelectedColor = themeManager.getDarkModeEnabled(getContext()) ? halfWhite : darkGray;
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        setColors();
        currentlySelected.setTextColor(selectedColor);
        if (homeButton != currentlySelected) {
            homeButton.setTextColor(nonSelectedColor);
        }
        if (searchButton != currentlySelected) {
            searchButton.setTextColor(nonSelectedColor);
        }
        if (folderButton != currentlySelected) {
            folderButton.setTextColor(nonSelectedColor);
        }
        if (homeButton != currentlySelected) {
            homeButton.setTextColor(nonSelectedColor);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @OnClick(R.id.home)
    public void onHomeClicked() {
        if (currentlySelected == homeButton) {
            return;
        }

        currentlySelected.setTextColor(nonSelectedColor);
        currentlySelected = homeButton;
        homeButton.setTextColor(selectedColor);
        listener.onNavItemSelected(R.id.home);
    }

    @OnClick(R.id.search)
    public void onSearchClicked() {
        if (currentlySelected == searchButton) {
            return;
        }

        currentlySelected.setTextColor(nonSelectedColor);
        currentlySelected = searchButton;
        searchButton.setTextColor(selectedColor);
        listener.onNavItemSelected(R.id.search);
    }

    @OnClick(R.id.add)
    public void onAddClicked() {
        if (isAddButtonExpanded) {
            listener.onAddOptionsContracted();
        } else {
            listener.onAddOptionsExpanded();
        }
        isAddButtonExpanded = !isAddButtonExpanded;
    }

    public void onAddSheetSlideOffset(float offset) {
        // Slide offset goes from -1 (hidden) to 0 (fully visible)
        // If offset is NaN (not a number), it's 0 (unsure why Google returns this value...)
        float adjustedValue = Float.valueOf(offset).isNaN() ? 1 : offset + 1.0f;
        float rotation = ADD_BUTTON_ROTATION_ANGLE * adjustedValue;
        addButton.setRotation(rotation);

        if (adjustedValue == 0) {
            isAddButtonExpanded = false;
        }
    }

    @OnClick(R.id.folders)
    public void onFoldersClicked() {
        if (currentlySelected == folderButton) {
            return;
        }

        currentlySelected.setTextColor(nonSelectedColor);
        currentlySelected = folderButton;
        folderButton.setTextColor(selectedColor);
        listener.onNavItemSelected(R.id.folders);
    }

    @OnClick(R.id.settings)
    public void onProfileClicked() {
        if (currentlySelected == settingsButton) {
            return;
        }

        currentlySelected.setTextColor(nonSelectedColor);
        currentlySelected = settingsButton;
        settingsButton.setTextColor(selectedColor);
        listener.onNavItemSelected(R.id.settings);
    }

    @Override
    public void onAttachedToWindow() {
        themeManager.registerListener(this);
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        themeManager.unregisterListener(this);
        super.onDetachedFromWindow();
    }
}
