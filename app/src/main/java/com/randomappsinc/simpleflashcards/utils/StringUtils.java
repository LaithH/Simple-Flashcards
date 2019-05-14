package com.randomappsinc.simpleflashcards.utils;

import android.util.Pair;

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

    /**
     *  Given a string that represents a line of a .csv file, break it up into
     *  the 2 strings that line represents (we're assuming the user formatted
     *  the flashcard set within their .csv file properly).
     */
    public static Pair<String, String> splitUpCsvLine(String line) {
        int commaIndex = -1;
        boolean startsWithQuote = line.startsWith("\"");
        boolean foundEndQuote = false;
        for (int i = (startsWithQuote ? 1 : 0); i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (startsWithQuote && currentChar == '\"') {
                foundEndQuote = true;
                continue;
            }
            if (currentChar == ',') {
                // If the line starts with a quote, we need to find the end quote
                // before registering the comma position (if the string has a comma
                // within it, the .csv wraps the entire value in quotes to identify that
                if (!startsWithQuote || foundEndQuote) {
                    commaIndex = i;
                    break;
                }
            }
        }
        String term;
        String definition = "";
        if (commaIndex == -1) {
            term = line;
        } else {
            int termStart = startsWithQuote ? 1 : 0;
            int termEnd = foundEndQuote ? commaIndex - 1 : commaIndex;
            term = line.substring(termStart, termEnd);
            definition = line.substring(commaIndex + 1);
        }
        return new Pair<>(term, definition);
    }
}
