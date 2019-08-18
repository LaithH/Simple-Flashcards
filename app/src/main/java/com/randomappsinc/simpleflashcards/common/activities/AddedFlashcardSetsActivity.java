package com.randomappsinc.simpleflashcards.common.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.adapters.AddedFlashcardSetsAdapter;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.models.FlashcardSetPreview;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows an overview of "imported" flashcard sets, either from nearby sharing or restoration
 */
public class AddedFlashcardSetsActivity extends StandardActivity {

    @BindView(R.id.added_sets) RecyclerView addedSetsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.added_flashcard_sets);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()
                .setHomeAsUpIndicator(new IconDrawable(this, IoniconsIcons.ion_android_close)
                        .colorRes(R.color.white)
                        .actionBarSize());

        List<FlashcardSetPreview> sets = getIntent().getParcelableArrayListExtra(Constants.ADDED_SET_PREVIEWS);
        AddedFlashcardSetsAdapter adapter = new AddedFlashcardSetsAdapter(listClickListener, sets);
        addedSetsList.setAdapter(adapter);
        addedSetsList.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    private final AddedFlashcardSetsAdapter.Listener listClickListener = setPreview -> {
        Intent intent = new Intent(AddedFlashcardSetsActivity.this, PlainSetViewActivity.class)
                .putExtra(Constants.SET_PREVIEW_KEY, setPreview);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }
}
