package com.randomappsinc.simpleflashcards.quiz.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.PictureFullViewActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.quiz.adapters.QuizResultsAdapter;
import com.randomappsinc.simpleflashcards.quiz.models.Problem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class QuizResultsFragment extends Fragment implements QuizResultsAdapter.Listener {

    public static QuizResultsFragment getInstance(
            ArrayList<Problem> problems,
            int numQuestions,
            boolean showWrongAnswers) {
        QuizResultsFragment quizResultsFragment = new QuizResultsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.PROBLEMS_KEY, problems);
        bundle.putInt(Constants.NUM_QUESTIONS_KEY, numQuestions);
        bundle.putBoolean(Constants.SHOW_WRONG_QUESTIONS_KEY, showWrongAnswers);
        quizResultsFragment.setArguments(bundle);
        return quizResultsFragment;
    }

    @BindView(R.id.no_problems) TextView noProblems;
    @BindView(R.id.quiz_results) RecyclerView quizResults;

    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.quiz_results_fragment,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);

        List<Problem> problems = getArguments().getParcelableArrayList(Constants.PROBLEMS_KEY);

        if (problems != null && !problems.isEmpty()) {
            noProblems.setVisibility(View.GONE);
            int numQuestions = getArguments().getInt(Constants.NUM_QUESTIONS_KEY);
            QuizResultsAdapter quizResultsAdapter = new QuizResultsAdapter(
                    problems,
                    numQuestions,
                    this);
            quizResults.setAdapter(quizResultsAdapter);
        } else {
            quizResults.setVisibility(View.GONE);
            boolean showWrongAnswers = getArguments().getBoolean(Constants.SHOW_WRONG_QUESTIONS_KEY);
            noProblems.setText(showWrongAnswers
                    ? R.string.none_wrong
                    : R.string.none_right);
            noProblems.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onImageClicked(Problem problem) {
        openFullImageView(problem);
    }

    protected void openFullImageView(Problem problem) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, PictureFullViewActivity.class)
                .putExtra(Constants.IMAGE_URL_KEY, problem.getQuestionImageUrl())
                .putExtra(Constants.CAPTION_KEY, problem.getQuestion());
        startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
