package com.randomappsinc.simpleflashcards.quiz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.Constants;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.quiz.constants.QuestionType;
import com.randomappsinc.simpleflashcards.quiz.models.QuizSettings;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class QuizSettingsActivity extends StandardActivity {

    @BindView(R.id.focus_sink_questions) View focusSinkQuestions;
    @BindView(R.id.focus_sink_minutes) View focusSinkMinutes;

    // Number of questions
    @BindView(R.id.num_questions) EditText numQuestions;

    // Time limit
    @BindView(R.id.no_time_limit) CheckBox noTimeLimit;
    @BindView(R.id.set_time_limit) CheckBox setTimeLimit;
    @BindView(R.id.num_minutes) EditText numMinutes;

    // Question types
    @BindView(R.id.multiple_choice_toggle) CheckBox multipleChoiceToggle;
    @BindView(R.id.free_form_input_toggle) CheckBox freeFormInputToggle;
    @BindView(R.id.terms_as_questions) CheckBox termsAsQuestionsToggle;
    @BindView(R.id.definitions_as_questions) CheckBox definitionsAsQuestionsToggle;

    private int numFlashcards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_settings);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()
                .setHomeAsUpIndicator(new IconDrawable(this, IoniconsIcons.ion_android_close)
                        .colorRes(R.color.white)
                        .actionBarSize());

        int flashcardSetId = getIntent().getIntExtra(Constants.FLASHCARD_SET_ID_KEY, 0);
        numFlashcards = DatabaseManager.get().getFlashcardSet(flashcardSetId).getFlashcards().size();
        numQuestions.setText(String.valueOf(numFlashcards));

        int numDigitsForQuestions = String.valueOf(numFlashcards).length();
        numQuestions.setFilters(new InputFilter[] {new InputFilter.LengthFilter(numDigitsForQuestions)});
    }

    @OnEditorAction(R.id.num_questions)
    public boolean onNumQuestionsEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            focusSinkQuestions.requestFocus();
            UIUtils.closeKeyboard(this);
            return true;
        }
        return false;
    }

    @OnEditorAction(R.id.num_minutes)
    public boolean onNumMinutesEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            focusSinkMinutes.requestFocus();
            UIUtils.closeKeyboard(this);
            return true;
        }
        return false;
    }

    @OnClick(R.id.minus_5_questions)
    public void remove5Questions() {
        int current = Integer.valueOf(numQuestions.getText().toString());
        current = Math.max(1, current - 5);
        numQuestions.setText(String.valueOf(current));
    }

    @OnClick(R.id.minus_1_question)
    public void remove1Question() {
        int current = Integer.valueOf(numQuestions.getText().toString());
        current = Math.max(1, current - 1);
        numQuestions.setText(String.valueOf(current));
    }

    @OnClick(R.id.plus_one_question)
    public void add1Question() {
        int current = Integer.valueOf(numQuestions.getText().toString());
        current = Math.min(numFlashcards, current + 1);
        numQuestions.setText(String.valueOf(current));
    }

    @OnClick(R.id.plus_5_questions)
    public void add5Questions() {
        int current = Integer.valueOf(numQuestions.getText().toString());
        current = Math.min(numFlashcards, current + 5);
        numQuestions.setText(String.valueOf(current));
    }

    @OnClick(R.id.minus_5_minutes)
    public void remove5Minutes() {
        int current = Integer.valueOf(numMinutes.getText().toString());
        current = Math.max(1, current - 5);
        numMinutes.setText(String.valueOf(current));
        setTimeLimit.setChecked(true);
    }

    @OnClick(R.id.minus_1_minute)
    public void remove1Minute() {
        int current = Integer.valueOf(numMinutes.getText().toString());
        current = Math.max(1, current - 1);
        numMinutes.setText(String.valueOf(current));
        setTimeLimit.setChecked(true);
    }

    @OnClick(R.id.plus_one_minute)
    public void add1Minute() {
        int current = Integer.valueOf(numMinutes.getText().toString());
        current = Math.min(999, current + 1);
        numMinutes.setText(String.valueOf(current));
        setTimeLimit.setChecked(true);
    }

    @OnClick(R.id.plus_5_minutes)
    public void add5Minutes() {
        int current = Integer.valueOf(numMinutes.getText().toString());
        current = Math.min(999, current + 5);
        numMinutes.setText(String.valueOf(current));
        setTimeLimit.setChecked(true);
    }

    @OnCheckedChanged(R.id.no_time_limit)
    public void noTimeLimitSelected(boolean isChecked) {
        if (isChecked) {
            noTimeLimit.setClickable(false);
            setTimeLimit.setChecked(false);
            setTimeLimit.setClickable(true);
        }
    }

    @OnCheckedChanged(R.id.set_time_limit)
    public void setTimeLimitSelected(boolean isChecked) {
        if (isChecked) {
            setTimeLimit.setClickable(false);
            noTimeLimit.setChecked(false);
            noTimeLimit.setClickable(true);
        }
    }

    @OnCheckedChanged(R.id.terms_as_questions)
    public void termsAsQuestionsSelected(boolean isChecked) {
        if (isChecked) {
            termsAsQuestionsToggle.setClickable(false);
            definitionsAsQuestionsToggle.setChecked(false);
            definitionsAsQuestionsToggle.setClickable(true);
        }
    }

    @OnCheckedChanged(R.id.definitions_as_questions)
    public void definitionsAsQuestionsSelected(boolean isChecked) {
        if (isChecked) {
            definitionsAsQuestionsToggle.setClickable(false);
            termsAsQuestionsToggle.setChecked(false);
            termsAsQuestionsToggle.setClickable(true);
        }
    }

    private boolean needsQuestionType() {
        return !multipleChoiceToggle.isChecked() && !freeFormInputToggle.isChecked();
    }

    private ArrayList<Integer> getChosenQuestionTypes() {
        ArrayList<Integer> questionTypes = new ArrayList<>();
        if (multipleChoiceToggle.isChecked()) {
            questionTypes.add(QuestionType.MULTIPLE_CHOICE);
        }
        if (freeFormInputToggle.isChecked()) {
            questionTypes.add(QuestionType.FREE_FORM_INPUT);
        }
        return questionTypes;
    }

    @OnClick(R.id.start_quiz)
    public void startQuiz() {
        if (needsQuestionType()) {
            UIUtils.showLongToast(R.string.question_type_needed, this);
            return;
        }

        int flashcardSetId = getIntent().getIntExtra(Constants.FLASHCARD_SET_ID_KEY, 0);

        // User theoretically could have requested more questions than the # of cards in the set or 0/null questions
        String numQuestionsText = numQuestions.getText().toString();
        if (TextUtils.isEmpty(numQuestionsText)) {
            UIUtils.showLongToast(R.string.question_number_needed, this);
            return;
        }
        int questionsValue = Integer.parseInt(numQuestionsText);
        if (questionsValue <= 0) {
            questionsValue = 1;
        } else if (questionsValue > numFlashcards) {
            questionsValue = numFlashcards;
        }

        int finalNumMinutes = 0;
        if (!noTimeLimit.isChecked()) {
            String numMinutesText = numMinutes.getText().toString();
            if (TextUtils.isEmpty(numMinutesText)) {
                UIUtils.showLongToast(R.string.time_limit_needed, this);
                return;
            }
            finalNumMinutes = Integer.valueOf(numMinutesText);
        }
        QuizSettings quizSettings = new QuizSettings(
                questionsValue,
                finalNumMinutes,
                getChosenQuestionTypes(),
                termsAsQuestionsToggle.isChecked());
        finish();
        startActivity(new Intent(
                this, QuizActivity.class)
                .putExtra(Constants.FLASHCARD_SET_ID_KEY, flashcardSetId)
                .putExtra(Constants.QUIZ_SETTINGS_KEY, quizSettings));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }
}
