package com.randomappsinc.simpleflashcards.dev;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DevFeatureTogglesActivity extends StandardActivity implements DevFeaturesTogglesAdapter.Listener {

    @BindView(R.id.feature_toggles) RecyclerView featuresList;

    private DevFeatureToggleManager featureToggleManager = DevFeatureToggleManager.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_feature_toggles);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        featuresList.addItemDecoration(new SimpleDividerItemDecoration(this));
        DevFeaturesTogglesAdapter adapter = new DevFeaturesTogglesAdapter(this);
        featuresList.setAdapter(adapter);
    }

    @Override
    public void onToggleClicked(String feature, boolean enabled) {
        featureToggleManager.setFeatureEnabled(this, feature, enabled);
    }
}
