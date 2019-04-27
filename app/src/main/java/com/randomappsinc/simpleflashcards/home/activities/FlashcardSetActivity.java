package com.randomappsinc.simpleflashcards.home.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.home.adapters.FlashcardSetOptionTabsAdapter;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

import androidx.viewpager.widget.ViewPager;
import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FlashcardSetActivity extends StandardActivity {

    @BindView(R.id.flashcard_set_name) TextView setName;
    @BindView(R.id.num_flashcards) TextView numCardsText;
    @BindView(R.id.flashcard_set_options_tabs) TabLayout tabs;
    @BindView(R.id.flashcard_set_options_pager) ViewPager viewPager;
    @BindArray(R.array.flashcard_set_option_categories) String[] tabTexts;

    private DatabaseManager databaseManager = DatabaseManager.get();
    private int setId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashcard_set_landing_page);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setId = getIntent().getIntExtra(Constants.FLASHCARD_SET_ID_KEY, 0);

        viewPager.setAdapter(new FlashcardSetOptionTabsAdapter(getSupportFragmentManager(), tabTexts, setId));
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    private void refreshView() {
        FlashcardSetDO flashcardSetDO = databaseManager.getFlashcardSet(setId);
        setName.setText(flashcardSetDO.getName());
        int numFlashcards = flashcardSetDO.getFlashcards().size();
        if (numFlashcards == 1) {
            numCardsText.setText(R.string.one_flashcard);
        } else {
            numCardsText.setText(getString(R.string.x_flashcards, numFlashcards));
        }
    }
}
