package com.randomappsinc.simpleflashcards.utils;

import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

import java.util.ArrayList;
import java.util.List;

public class FlashcardUtils {

    public static double getLearnedPercent(FlashcardSetDO flashcardSet) {
        List<FlashcardDO> flashcardList = flashcardSet.getFlashcards();
        double totalFlashcards = flashcardList.size();
        double percentLearned = 0;
        // Prevent divide by 0
        if (totalFlashcards > 0) {
            double numLearned = 0;
            for (FlashcardDO flashcardDO : flashcardList) {
                if (flashcardDO.isLearned()) {
                    numLearned++;
                }
            }
            percentLearned = (numLearned / totalFlashcards) * 100.0f;
        }
        return percentLearned;
    }

    public static List<FlashcardDO> getFilteredFlashcards(FlashcardSetDO flashcardSetDO, boolean onlyGetNotLearned) {
        List<FlashcardDO> originalCards = flashcardSetDO.getFlashcards();
        if (onlyGetNotLearned) {
            List<FlashcardDO> filteredFlashcards = new ArrayList<>();
            for (FlashcardDO flashcardDO : originalCards) {
                if (!flashcardDO.isLearned()) {
                    filteredFlashcards.add(flashcardDO);
                }
            }
            return filteredFlashcards;
        }
        return originalCards;
    }
}
