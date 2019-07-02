package com.randomappsinc.simpleflashcards.utils;

import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

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
}
