package com.randomappsinc.simpleflashcards.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.randomappsinc.simpleflashcards.R;

public class ThemedSwitch extends SwitchCompat implements ThemeManager.Listener {

    private ThemeManager themeManager;
    private int uncheckedColor;
    private int uncheckedColorDarkMode;
    private int checkedColor;
    private int checkedColorDarkMode;

    public ThemedSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        themeManager = ThemeManager.get();
        uncheckedColor = ContextCompat.getColor(context, R.color.gray_400);
        uncheckedColorDarkMode = ContextCompat.getColor(context, R.color.gray);
        checkedColor = ContextCompat.getColor(context, R.color.light_blue);
        checkedColorDarkMode = ContextCompat.getColor(context, R.color.dark_blue);

        setTrackColor(themeManager.getDarkModeEnabled(context));
    }

    private void setTrackColor(boolean darkModeEnabled) {
        DrawableCompat.setTintList(getTrackDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        darkModeEnabled ? checkedColorDarkMode : checkedColor,
                        darkModeEnabled ? uncheckedColorDarkMode : uncheckedColor
                }));
    }

    public void setProperColors() {
        setTrackColor(themeManager.getDarkModeEnabled(getContext()));
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        setTrackColor(darkModeEnabled);
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
