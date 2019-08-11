package com.randomappsinc.simpleflashcards.common.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.randomappsinc.simpleflashcards.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/** Custom radio button with border */
public class BetterRadioButton extends LinearLayout  {

    @BindView(R.id.radio_button) RadioButton radioButton;
    @BindView(R.id.radio_button_text) TextView textView;

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
        ButterKnife.bind(this);
    }

    public void setText(String text) {
        textView.setText(text);
    }
}
