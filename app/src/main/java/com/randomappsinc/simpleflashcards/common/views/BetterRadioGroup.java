package com.randomappsinc.simpleflashcards.common.views;

import android.content.Context;
import android.util.AttributeSet;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemedLinearLayout;

import java.util.ArrayList;
import java.util.List;

public class BetterRadioGroup extends ThemedLinearLayout {

    private List<BetterRadioButton> radioButtons = new ArrayList<>();

    public BetterRadioGroup(Context context) {
        this(context, null);
    }

    public BetterRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setSize(int numButtons) {
        for (int i = 0; i < numButtons; i++) {
            BetterRadioButton radioButton = new BetterRadioButton(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            int spacing = getContext().getResources().getDimensionPixelSize(R.dimen.radio_group_button_spacing);
            params.setMargins(0, spacing, 0, spacing);
            radioButton.setLayoutParams(params);
            radioButtons.add(radioButton);
            addView(radioButton);
        }
    }

    public BetterRadioButton getRadioButton(int index) {
        return radioButtons.get(index);
    }
}
