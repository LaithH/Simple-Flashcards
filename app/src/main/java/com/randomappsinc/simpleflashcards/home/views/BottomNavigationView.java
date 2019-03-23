package com.randomappsinc.simpleflashcards.home.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;

import androidx.annotation.IdRes;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomNavigationView extends LinearLayout implements ThemeManager.Listener {

    public interface Listener {
        void onNavItemSelected(@IdRes int viewId);
    }

    @BindView(R.id.home) TextView homeButton;
    @BindView(R.id.search) TextView searchButton;
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
