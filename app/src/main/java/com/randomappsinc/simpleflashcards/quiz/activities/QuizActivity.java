package com.randomappsinc.simpleflashcards.quiz.activities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.PictureFullViewActivity;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.dialogs.ConfirmQuitDialog;
import com.randomappsinc.simpleflashcards.common.views.BetterRadioGroup;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.quiz.constants.QuestionType;
import com.randomappsinc.simpleflashcards.quiz.constants.QuizScore;
import com.randomappsinc.simpleflashcards.quiz.managers.TimerManager;
import com.randomappsinc.simpleflashcards.quiz.models.Problem;
import com.randomappsinc.simpleflashcards.quiz.models.Quiz;
import com.randomappsinc.simpleflashcards.quiz.models.QuizSettings;
import com.randomappsinc.simpleflashcards.utils.UIUtils;
import com.randomappsinc.simpleflashcards.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuizActivity extends StandardActivity implements ConfirmQuitDialog.Listener {

    @BindView(R.id.problem_parent) ScrollView problemParent;
    @BindView(R.id.question_header) TextView questionHeader;
    @BindView(R.id.question) TextView questionText;
    @BindView(R.id.question_image) ImageView questionImage;

    @BindView(R.id.options) RadioGroup optionsContainer;
    @BindViews({R.id.option_1, R.id.option_2, R.id.option_3, R.id.option_4}) List<RadioButton> optionButtons;

    @BindView(R.id.radio_button_group) BetterRadioGroup radioButtonGroup;

    @BindView(R.id.answer_input) EditText answerInput;
    @BindView(R.id.submit) View submitButton;
    @BindView(R.id.results_page) View resultsPage;
    @BindView(R.id.results_header) TextView resultsHeader;
    @BindView(R.id.score) TextView score;

    @BindString(R.string.quiz_question_header) String headerTemplate;
    @BindString(R.string.good_score_message) String goodScore;
    @BindString(R.string.okay_score_message) String okayScore;
    @BindString(R.string.bad_score_message) String badScore;
    @BindString(R.string.your_score_was) String scoreHeaderTemplate;
    @BindString(R.string.quiz_score_template) String scoreTemplate;
    @BindInt(R.integer.shorter_anim_length) int animationLength;

    private FlashcardSetDO flashcardSet;
    private Quiz quiz;
    private ConfirmQuitDialog confirmQuitDialog;
    private QuizSettings quizSettings;
    @Nullable private TimerManager timerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int setId = getIntent().getIntExtra(Constants.FLASHCARD_SET_ID_KEY, 0);
        flashcardSet = DatabaseManager.get().getFlashcardSet(setId);
        quizSettings = getIntent().getParcelableExtra(Constants.QUIZ_SETTINGS_KEY);
        if (quizSettings.getNumSeconds() <= 0) {
            setTitle(flashcardSet.getName());
        } else {
            timerManager = new TimerManager(timerListener, quizSettings.getNumSeconds());
        }

        confirmQuitDialog = new ConfirmQuitDialog(
                this, this, R.string.confirm_quiz_exit_body);

        quiz = new Quiz(flashcardSet, quizSettings);
        int numOptions = quiz.getNumOptions();
        if (numOptions >= 3) {
            optionButtons.get(2).setVisibility(View.VISIBLE);
        }
        if (numOptions >= 4) {
            optionButtons.get(3).setVisibility(View.VISIBLE);
        }

        radioButtonGroup.setSize(quiz.getNumOptions());

        loadCurrentQuestionIntoView();
    }

    private final TimerManager.Listener timerListener = new TimerManager.Listener() {
        @Override
        public void onTimeUpdated(String time) {
            setTitle(time);
        }

        @Override
        public void onTimeUp() {
            fadeOutProblemPage();
        }
    };

    protected void loadCurrentQuestionIntoView() {
        // Uncheck currently chosen option if applicable
        RadioButton chosenButton = getChosenButton();
        if (chosenButton != null) {
            optionsContainer.clearCheck();
            optionsContainer.jumpDrawablesToCurrentState();
        }
        String headerText = String.format(
                headerTemplate,
                quiz.getCurrentProblemPosition() + 1,
                quiz.getNumQuestions());
        questionHeader.setText(headerText);
        Problem problem = quiz.getCurrentProblem();
        questionText.setText(problem.getQuestion());

        final String imageUrl = problem.getQuestionImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            questionImage.setVisibility(View.VISIBLE);
            if (ViewCompat.isLaidOut(questionImage)) {
                loadImage(imageUrl);
            } else {
                ViewUtils.runOnPreDraw(questionImage, new Runnable() {
                    @Override
                    public void run() {
                        loadImage(imageUrl);
                    }
                });
            }
        } else {
            questionImage.setVisibility(View.GONE);
        }

        switch (quiz.getCurrentProblem().getQuestionType()) {
            case QuestionType.MULTIPLE_CHOICE:
                answerInput.setVisibility(View.GONE);
                List<String> options = problem.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    optionButtons.get(i).setText(options.get(i));
                }
                optionsContainer.setVisibility(View.VISIBLE);
                break;
            case QuestionType.FREE_FORM_INPUT:
                optionsContainer.setVisibility(View.GONE);
                answerInput.setText("");
                answerInput.setVisibility(View.VISIBLE);
                break;
        }
    }

    protected void loadImage(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .resize(0, questionImage.getHeight())
                .into(questionImage);
    }

    @OnClick(R.id.question_image)
    public void openImageInFullView() {
        String imageUrl = quiz.getCurrentProblem().getQuestionImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Intent intent = new Intent(this, PictureFullViewActivity.class)
                    .putExtra(Constants.IMAGE_URL_KEY, imageUrl)
                    .putExtra(Constants.CAPTION_KEY, quiz.getCurrentProblem().getQuestion());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, 0);
        }
    }

    private void animateQuestionOut() {
        submitButton.setEnabled(false);
        problemParent
                .animate()
                .translationXBy(-1 * problemParent.getWidth())
                .alpha(0)
                .setDuration(animationLength)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        problemParent.setTranslationX(0);
                        animationQuestionIn();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    protected void animationQuestionIn() {
        problemParent
                .animate()
                .alpha(1)
                .setDuration(animationLength)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        loadCurrentQuestionIntoView();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        submitButton.setEnabled(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    @OnClick(R.id.submit)
    public void submitAnswer() {
        if (quiz.isQuizComplete()) {
            return;
        }

        // Verify the user's input and extract answer
        switch (quiz.getCurrentProblem().getQuestionType()) {
            case QuestionType.MULTIPLE_CHOICE:
                RadioButton chosenButton = getChosenButton();
                if (chosenButton == null) {
                    UIUtils.showLongToast(R.string.please_check_something, this);
                    return;
                } else {
                    quiz.submitAnswer(chosenButton.getText().toString());
                }
                break;
            case QuestionType.FREE_FORM_INPUT:
                String input = answerInput.getText().toString().trim();
                if (input.isEmpty()) {
                    UIUtils.showLongToast(R.string.please_enter_in_something, this);
                    return;
                } else {
                    UIUtils.closeKeyboard(this);
                    quiz.submitAnswer(input);
                }
                break;
        }

        problemParent.fullScroll(ScrollView.FOCUS_UP);
        quiz.advanceToNextProblem();
        if (quiz.isQuizComplete()) {
            if (timerManager != null) {
                timerManager.stopTimer();
            }
            fadeOutProblemPage();
        } else {
            animateQuestionOut();
        }
    }

    protected void fadeOutProblemPage() {
        problemParent
                .animate()
                .alpha(0)
                .setDuration(animationLength)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        problemParent.setVisibility(View.GONE);
                        submitButton.setVisibility(View.GONE);
                        fadeInResultsPage();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
        submitButton.animate().alpha(0).setDuration(animationLength);
    }

    protected void loadResultsIntoView() {
        Quiz.Grade grade = quiz.getGrade();
        String quizScore = "";
        switch (grade.getScore()) {
            case QuizScore.GOOD:
                quizScore = goodScore;
                break;
            case QuizScore.OKAY:
                quizScore = okayScore;
                break;
            case QuizScore.BAD:
                quizScore = badScore;
                break;
        }
        String scoreHeaderText = String.format(
                Locale.getDefault(),
                scoreHeaderTemplate,
                quizScore);
        resultsHeader.setText(scoreHeaderText);
        String scoreText = String.format(
                Locale.getDefault(),
                scoreTemplate,
                grade.getFractionText(),
                grade.getPercentText());
        score.setText(scoreText);
    }

    protected void fadeInResultsPage() {
        resultsPage
                .animate()
                .alpha(1)
                .setDuration(animationLength)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        loadResultsIntoView();
                        resultsPage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {}

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    private void fadeOutResultsPage() {
        resultsPage
                .animate()
                .alpha(0)
                .setDuration(animationLength)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resultsPage.setVisibility(View.GONE);
                        fadeInProblemPage();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    protected void fadeInProblemPage() {
        problemParent
                .animate()
                .alpha(1)
                .setDuration(animationLength)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        loadCurrentQuestionIntoView();
                        problemParent.setVisibility(View.VISIBLE);
                        submitButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {}

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
        submitButton.animate().alpha(1).setDuration(animationLength);
    }

    @OnClick(R.id.retake)
    public void retake() {
        quiz = new Quiz(flashcardSet, quizSettings);
        if (timerManager != null) {
            timerManager.resetAndStart();
        }
        fadeOutResultsPage();
    }

    @OnClick(R.id.exit)
    public void exit() {
        finish();
    }

    @OnClick(R.id.view_results)
    public void viewResults() {
        Intent intent = new Intent(this, QuizResultsActivity.class)
                .putParcelableArrayListExtra(Constants.QUIZ_RESULTS_KEY, quiz.getProblems());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
    }

    @Nullable
    private RadioButton getChosenButton() {
        for (RadioButton radioButton : optionButtons) {
            if (radioButton.isChecked()) {
                return radioButton;
            }
        }
        return null;
    }

    private void onQuizExit() {
        if (!quiz.isQuizComplete()) {
            confirmQuitDialog.show();
        } else {
            finish();
        }
    }

    @Override
    public void onQuitConfirmed() {
        finish();
    }

    @Override
    public void onBackPressed() {
        onQuizExit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (timerManager != null) {
            timerManager.resumeTimer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timerManager != null) {
            timerManager.pauseTimer();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (timerManager != null) {
            timerManager.finish();
        }
        confirmQuitDialog.cleanUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onQuizExit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
