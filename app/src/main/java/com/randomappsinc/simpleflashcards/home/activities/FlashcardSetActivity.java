package com.randomappsinc.simpleflashcards.home.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.home.adapters.FlashcardSetOptionTabsAdapter;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FlashcardSetActivity extends StandardActivity {

    public static final int IMPORT_CODE = 5;

    @BindView(R.id.flashcard_set_name) TextView setName;
    @BindView(R.id.num_flashcards) TextView numCardsText;
    @BindView(R.id.percent_view) TextView percentText;
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

    public void refreshView() {
        FlashcardSetDO flashcardSet = databaseManager.getFlashcardSet(setId);
        setTitle(flashcardSet.getName());
        setName.setText(flashcardSet.getName());
        int numFlashcards = flashcardSet.getFlashcards().size();
        if (numFlashcards == 1) {
            numCardsText.setText(R.string.one_flashcard);
        } else {
            numCardsText.setText(getString(R.string.x_flashcards, numFlashcards));
        }

        List<FlashcardDO> flashcardList = flashcardSet.getFlashcards();
        double totalFlashcards = flashcardList.size();
        double numLearned = 0;
        for (FlashcardDO flashcardDO : flashcardList) {
            if (flashcardDO.isLearned()) {
                numLearned++;
            }
        }
        double percentLearned = (numLearned / totalFlashcards) * 100.0f;
        String percentString = new DecimalFormat("#.#").format(percentLearned);
        percentText.setText(getString(R.string.percent_string, percentString));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == IMPORT_CODE && resultCode == RESULT_OK) {
            refreshView();
        }
    }
}
