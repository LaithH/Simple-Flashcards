package com.randomappsinc.simpleflashcards.quiz.models;

import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.quiz.constants.QuestionType;
import com.randomappsinc.simpleflashcards.quiz.constants.QuizScore;
import com.randomappsinc.simpleflashcards.utils.FlashcardUtils;
import com.randomappsinc.simpleflashcards.utils.RandUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Quiz {

    private static final float GOOD_PERCENTAGE_THRESHOLD = 80;
    private static final float OKAY_PERCENTAGE_THRESHOLD = 60;

    private ArrayList<Problem> problems;
    private int currentProblem = 0;
    private int numOptions;

    public class Grade {
        private @QuizScore int score = QuizScore.BAD;
        private String fractionText;
        private String percentText;

        public @QuizScore int getScore() {
            return score;
        }

        void setScore(@QuizScore int score) {
            this.score = score;
        }

        public String getFractionText() {
            return fractionText;
        }

        void setFractionText(String fractionText) {
            this.fractionText = fractionText;
        }

        public String getPercentText() {
            return percentText;
        }

        void setPercentText(String percentText) {
            this.percentText = percentText;
        }
    }

    public Quiz(FlashcardSetDO flashcardSet, QuizSettings quizSettings) {
        problems = new ArrayList<>();
        List<FlashcardDO> flashcards = FlashcardUtils.getFilteredFlashcards(
                flashcardSet, quizSettings.onlyUseNotLearned());
        numOptions = Math.min(flashcards.size(), Problem.NORMAL_NUM_ANSWER_OPTIONS);

        int numQuestions = quizSettings.getNumQuestions();
        List<Integer> questionTypes = quizSettings.getQuestionTypes();
        boolean useTermsAsQuestions = quizSettings.useTermsAsQuestions();

        // Indexes of the flashcards we are generating questions for
        List<Integer> indexes = RandUtils.getProblemIndexes(flashcards.size(), numQuestions);

        int currentQuestionPosition = 1;
        Random random = new Random();
        for (int index : indexes) {
            FlashcardDO flashcard = flashcards.get(index);
            Problem problem = new Problem(currentQuestionPosition);
            int questionTypeIndex = random.nextInt(questionTypes.size());
            switch (questionTypes.get(questionTypeIndex)) {
                case QuestionType.MULTIPLE_CHOICE:
                    problem.setAsMultipleChoiceQuestion(flashcard, flashcards, useTermsAsQuestions);
                    break;
                case QuestionType.FREE_FORM_INPUT:
                    problem.setAsFreeFormInputQuestion(flashcard, useTermsAsQuestions);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported question type");
            }
            problems.add(problem);
            currentQuestionPosition++;
        }
    }

    public void advanceToNextProblem() {
        currentProblem++;
    }

    public void submitAnswer(String answer) {
        problems.get(currentProblem).setGivenAnswer(answer);
    }

    public Problem getCurrentProblem() {
        return problems.get(currentProblem);
    }

    public int getNumOptions() {
        return numOptions;
    }

    public boolean isQuizComplete() {
        return currentProblem >= problems.size();
    }

    public int getCurrentProblemPosition() {
        return currentProblem;
    }

    public int getNumQuestions() {
        return problems.size();
    }

    public Grade getGrade() {
        Grade grade = new Grade();
        int numCorrect = 0;
        for (Problem problem : problems) {
            if (problem.wasUserCorrect()) {
                numCorrect++;
            }
        }
        int totalQuestions = problems.size();
        grade.setFractionText(String.format(Locale.getDefault(), "%d/%d", numCorrect, totalQuestions));
        float percentage = ((float) numCorrect / (float) totalQuestions) * 100.0f;
        grade.setPercentText(String.format(Locale.getDefault(), "%.2f", percentage) + "%");
        if (percentage >= GOOD_PERCENTAGE_THRESHOLD) {
            grade.setScore(QuizScore.GOOD);
        } else if (percentage >= OKAY_PERCENTAGE_THRESHOLD) {
            grade.setScore(QuizScore.OKAY);
        }
        return grade;
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }
}
