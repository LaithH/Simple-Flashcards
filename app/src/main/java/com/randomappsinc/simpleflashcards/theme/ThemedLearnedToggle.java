package com.randomappsinc.simpleflashcards.theme;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.joanzapata.iconify.widget.IconTextView;
import com.randomappsinc.simpleflashcards.R;

public class ThemedLearnedToggle extends IconTextView implements ThemeManager.Listener {

    private ThemeManager themeManager;
    private final int normalNotLearnedTextColor;
    private final int normalLearnedTextColor;
    private final int darkModeTextColor;
    private final Drawable normalNotLearnedBackground;
    private final Drawable darkNotLearnedBackground;
    private final Drawable learnedBackground;
    private boolean learned;

    public ThemedLearnedToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        themeManager = ThemeManager.get();
        normalNotLearnedTextColor = ContextCompat.getColor(context, R.color.app_blue);
        normalLearnedTextColor = ContextCompat.getColor(context, R.color.white);
        darkModeTextColor = ContextCompat.getColor(context, R.color.white);
        normalNotLearnedBackground = ContextCompat.getDrawable(context, R.drawable.rounded_blue_border);
        darkNotLearnedBackground = ContextCompat.getDrawable(context, R.drawable.rounded_white_border);
        learnedBackground = ContextCompat.getDrawable(context, R.drawable.rounded_blue_rectangle);
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        setProperColors(darkModeEnabled);
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
        setProperColors(themeManager.getDarkModeEnabled(getContext()));
    }

    public void setProperColors(boolean darkModeEnabled) {
        if (learned) {
            setText(R.string.learned_with_icon);
            setTextColor(darkModeEnabled ? darkModeTextColor : normalLearnedTextColor);
            setBackground(learnedBackground);
        } else {
            setText(R.string.not_learned);
            setTextColor(darkModeEnabled ? darkModeTextColor : normalNotLearnedTextColor);
            setBackground(darkModeEnabled ? darkNotLearnedBackground : normalNotLearnedBackground);
        }
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