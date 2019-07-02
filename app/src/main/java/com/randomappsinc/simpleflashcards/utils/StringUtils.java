package com.randomappsinc.simpleflashcards.utils;

import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;

import java.text.DecimalFormat;
import java.util.HashMap;

public class StringUtils {

    public static String getSaneDeviceString(String endpointName) {
        int newlinePos = endpointName.indexOf("\n");
        if (newlinePos == -1 || newlinePos == endpointName.length() - 1) {
            return endpointName;
        } else {
            String nearbyName = endpointName.substring(0, newlinePos);
            String deviceType = endpointName.substring(newlinePos + 1);
            return nearbyName + " (" + deviceType + ")";
        }
    }

    public static HashMap<String, Integer> getWordAmounts(String[] splits) {
        HashMap<String, Integer> wordAmounts = new HashMap<>();
        for (String answerWord : splits) {
            String cleanWord = answerWord.toLowerCase();
            if (wordAmounts.containsKey(cleanWord)) {
                int currentAmount = wordAmounts.get(cleanWord);
                wordAmounts.put(cleanWord, currentAmount + 1);
            } else {
                wordAmounts.put(cleanWord, 1);
            }
        }
        return wordAmounts;
    }

    /**
     *  Given a string of words, capitalizes the first letter in each word
     *  and lowercases the rest.
     *
     *  @param givenString The input string
     *  @return The formatted string
     */
    public static String capitalizeFirstWord(String givenString) {
        String[] words = givenString.split(" ");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            String trimmed = word.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            char firstChar = capitalizedWords.length() == 0
                    ? Character.toUpperCase(trimmed.charAt(0))
                    : trimmed.charAt(0);
            capitalizedWords
                    .append(firstChar)
                    .append(word.substring(1))
                    .append(" ");
        }
        return capitalizedWords.toString().trim();
    }

    public static String getSetPercentLearnedText(FlashcardSetDO flashcardSet) {
        return new DecimalFormat("#.#").format(FlashcardUtils.getLearnedPercent(flashcardSet));
    }
}
