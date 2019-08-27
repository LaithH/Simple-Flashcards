package com.randomappsinc.simpleflashcards.common.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemedLinearLayout;

import java.util.ArrayList;
import java.util.List;

public class BetterRadioGroup extends ThemedLinearLayout implements BetterRadioButton.Listener {

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
            radioButton.setListener(this);
            radioButton.setIndex(i);
            addView(radioButton);
        }
    }

    @Nullable
    public BetterRadioButton getCheckedButton() {
        for (BetterRadioButton radioButton : radioButtons) {
            if (radioButton.isChecked()) {
                return radioButton;
            }
        }
        return null;
    }

    public void clearAllChecks() {
        for (BetterRadioButton radioButton : radioButtons) {
            if (radioButton.isChecked()) {
                radioButton.clearCheckImmediately();
            }
        }
    }

    public BetterRadioButton getRadioButton(int index) {
        return radioButtons.get(index);
    }

    @Override
    public void onChecked(int index) {
        for (int i = 0; i < radioButtons.size(); i++) {
            if (i != index) {
                radioButtons.get(i).setChecked(false);
            }
        }
    }
}
