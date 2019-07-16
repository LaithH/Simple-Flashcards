package com.randomappsinc.simpleflashcards.quiz.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.quiz.models.Problem;
import com.randomappsinc.simpleflashcards.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuizResultsAdapter extends RecyclerView.Adapter<QuizResultsAdapter.QuizResultViewHolder> {

    public interface Listener {
        void onImageClicked(Problem problem);
    }

    protected List<Problem> problems;
    protected int numProblems;
    protected Listener listener;

    public QuizResultsAdapter(
            List<Problem> problems,
            int numProblems,
            @NonNull Listener listener) {
        this.problems = problems;
        this.numProblems = numProblems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.quiz_result_item_cell,
                parent,
                false);
        return new QuizResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizResultViewHolder holder, int position) {
        holder.loadResult(position);
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    class QuizResultViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.question_header) TextView questionHeader;
        @BindView(R.id.question) TextView question;
        @BindView(R.id.question_image) ImageView questionImage;
        @BindView(R.id.your_answer_icon) TextView yourAnswerIcon;
        @BindView(R.id.your_answer) TextView yourAnswer;
        @BindView(R.id.correct_answer_header) View correctAnswerHeader;
        @BindView(R.id.correct_answer_container) View correctAnswerContainer;
        @BindView(R.id.correct_answer) TextView correctAnswer;

        @BindColor(R.color.green) int green;
        @BindColor(R.color.red) int red;
        @BindString(R.string.quiz_question_header) String headerTemplate;
        @BindString(R.string.unsubmitted) String unsubmitted;

        QuizResultViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadResult(int position) {
            Problem problem = problems.get(position);
            String headerText = String.format(headerTemplate, problem.getQuestionNumber(), numProblems);
            questionHeader.setText(headerText);
            question.setText(problem.getQuestion());

            final String imageUrl = problem.getQuestionImageUrl();
            if (!TextUtils.isEmpty(imageUrl)) {
                questionImage.setVisibility(View.VISIBLE);
                if (ViewCompat.isLaidOut(questionImage)) {
                    loadImage(imageUrl);
                } else {
                    ViewUtils.runOnPreDraw(questionImage, () -> loadImage(imageUrl));
                }
            } else {
                questionImage.setVisibility(View.GONE);
            }

            boolean wasUserCorrect = problem.wasUserCorrect();
            yourAnswerIcon.setText(wasUserCorrect ? R.string.check_icon : R.string.x_icon);
            yourAnswerIcon.setTextColor(wasUserCorrect ? green : red);
            String givenAnswer = problem.getGivenAnswer();
            yourAnswer.setText(TextUtils.isEmpty(givenAnswer) ? unsubmitted : givenAnswer);

            if (wasUserCorrect) {
                correctAnswerHeader.setVisibility(View.GONE);
                correctAnswerContainer.setVisibility(View.GONE);
            } else {
                correctAnswer.setText(problem.getAnswer());
                correctAnswerHeader.setVisibility(View.VISIBLE);
                correctAnswerContainer.setVisibility(View.VISIBLE);
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
            listener.onImageClicked(problems.get(getAdapterPosition()));
        }
    }
}
