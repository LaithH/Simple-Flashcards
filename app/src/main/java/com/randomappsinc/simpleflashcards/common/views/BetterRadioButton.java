package com.randomappsinc.simpleflashcards.common.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/** Custom radio button with border */
public class BetterRadioButton extends LinearLayout implements ThemeManager.Listener, View.OnClickListener {

    public interface Listener {
        void onChecked(int index);
    }

    @BindView(R.id.radio_button) RadioButton radioButton;
    @BindView(R.id.radio_button_text) TextView textView;

    // Position of the radio button within the radio group
    private int index;

    private ThemeManager themeManager = ThemeManager.get();
    private @Nullable Listener listener;

    public BetterRadioButton(Context context) {
        this(context, null, 0);
    }

    public BetterRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BetterRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(getContext(), R.layout.better_radio_button, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.rounded_blue_border);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.radio_button_padding);
        setPadding(0, padding, padding, padding);
        ButterKnife.bind(this);
        setBackground();
        setOnClickListener(this);
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setChecked(boolean isChecked) {
        radioButton.setChecked(isChecked);
    }

    public boolean isChecked() {
        return radioButton.isChecked();
    }

    public void clearCheckImmediately() {
        UIUtils.setCheckedImmediately(radioButton, false);
    }

    @OnCheckedChanged(R.id.radio_button)
    public void onCheckChanged() {
        setBackground();
        if (radioButton.isChecked() && listener != null) {
            listener.onChecked(index);
        }
    }

    public String getText() {
        return textView.getText().toString();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    @Override
    public void onClick(View view) {
        if (!radioButton.isChecked()) {
            radioButton.setChecked(true);
            setBackground();
        }
    }

    private void setBackground() {
        if (radioButton.isChecked()) {
            setBackgroundResource(R.drawable.rounded_blue_border);
        } else {
            setBackgroundResource(themeManager.getDarkModeEnabled(getContext())
                    ? R.drawable.rounded_white_border
                    : R.drawable.rounded_gray_border);
        }
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        setBackground();
    }

    @Override
    public void onAttachedToWindow() {
        themeManager.registerListener(this);
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        themeManager.unregisterListener(this);
        listener = null;
        super.onDetachedFromWindow();
    }
}
