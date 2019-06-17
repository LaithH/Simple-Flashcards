package com.randomappsinc.simpleflashcards.home.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.randomappsinc.simpleflashcards.home.fragments.EditFlashcardSetFragment;
import com.randomappsinc.simpleflashcards.home.fragments.FlashcardSetMoreOptionsFragment;
import com.randomappsinc.simpleflashcards.home.fragments.LearnFlashcardSetFragment;

public class FlashcardSetOptionTabsAdapter extends FragmentPagerAdapter {

    private String[] optionTabs;
    private int setId;

    public FlashcardSetOptionTabsAdapter(FragmentManager fragmentManager, String[] tabNames, int setId) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.optionTabs = tabNames;
        this.setId = setId;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return LearnFlashcardSetFragment.getInstance(setId);
            case 1:
                return EditFlashcardSetFragment.getInstance(setId);
            case 2:
                return FlashcardSetMoreOptionsFragment.getInstance(setId);
            default:
                throw new IllegalStateException("Unsupported index for set options: " + position);
        }
    }

    @Override
    public int getCount() {
        return optionTabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return optionTabs[position];
    }
}

