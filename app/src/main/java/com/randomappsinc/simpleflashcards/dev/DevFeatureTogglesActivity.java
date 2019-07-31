package com.randomappsinc.simpleflashcards.dev;

import android.os.Bundle;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;

import butterknife.ButterKnife;

public class DevFeatureTogglesActivity extends StandardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_feature_toggles);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
