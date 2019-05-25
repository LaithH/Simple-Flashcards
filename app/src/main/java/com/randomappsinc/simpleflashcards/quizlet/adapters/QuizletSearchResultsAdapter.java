package com.randomappsinc.simpleflashcards.quizlet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletSetResult;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;
import com.randomappsinc.simpleflashcards.theme.ThemedCardView;
import com.randomappsinc.simpleflashcards.theme.ThemedExtrasTextView;
import com.randomappsinc.simpleflashcards.theme.ThemedIconTextView;
import com.randomappsinc.simpleflashcards.theme.ThemedSubtitleTextView;
import com.randomappsinc.simpleflashcards.theme.ThemedTextView;
import com.randomappsinc.simpleflashcards.theme.ThemedTitleTextView;
import com.randomappsinc.simpleflashcards.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuizletSearchResultsAdapter
        extends RecyclerView.Adapter<QuizletSearchResultsAdapter.ResultViewHolder>
        implements ThemeManager.Listener {

    public interface Listener {
        void onResultClicked(QuizletSetResult result);
    }

    protected Context context;
    protected Listener listener;
    protected List<QuizletSetResult> results = new ArrayList<>();
    protected boolean paginationEnabled;
    protected DatabaseManager databaseManager = DatabaseManager.get();
    private ThemeManager themeManager = ThemeManager.get();

    public QuizletSearchResultsAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        themeManager.registerListener(this);
    }

    public void clear() {
        results.clear();
        notifyDataSetChanged();
    }

    public void addFlashcardSets(List<QuizletSetResult> results, boolean paginationEnabled) {
        this.results.addAll(results);
        this.paginationEnabled = paginationEnabled;
        notifyDataSetChanged();
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        notifyDataSetChanged();
    }

    public void cleanUp() {
        themeManager.unregisterListener(this);
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(
                R.layout.quizlet_search_result_cell,
                parent,
                false);
        return new ResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.loadResult(position);
    }

    @Override
    public int getItemCount() {
        int paginationAddition = paginationEnabled ? 1 : 0;
        return results.size() + paginationAddition;
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_parent) ThemedCardView card;
        @BindView(R.id.title) ThemedTitleTextView title;
        @BindView(R.id.num_flashcards) ThemedSubtitleTextView numFlashcardsText;
        @BindView(R.id.created_on) ThemedExtrasTextView createdOn;
        @BindView(R.id.last_updated) ThemedExtrasTextView lastUpdated;
        @BindView(R.id.side_info) View sideInfo;
        @BindView(R.id.images_icon) ThemedIconTextView imageIcon;
        @BindView(R.id.images_text) ThemedTextView imageText;
        @BindView(R.id.in_library) View inLibraryText;
        @BindView(R.id.loading_spinner) View loadingSpinner;

        ResultViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadResult(int position) {
            if (paginationEnabled && position == getItemCount() - 1) {
                card.setVisibility(View.GONE);
                loadingSpinner.setVisibility(View.VISIBLE);
            } else {
                loadingSpinner.setVisibility(View.GONE);
                card.setVisibility(View.VISIBLE);

                QuizletSetResult result = results.get(position);
                title.setText(result.getTitle());
                int numFlashcards = result.getFlashcardCount();
                if (numFlashcards == 1) {
                    numFlashcardsText.setText(R.string.one_flashcard);
                } else {
                    numFlashcardsText.setText(context.getString(R.string.x_flashcards, numFlashcards));
                }
                createdOn.setText(context.getString(
                        R.string.created_on_template,
                        TimeUtils.getFlashcardSetTime(result.getCreatedDate())));
                lastUpdated.setText(context.getString(
                        R.string.last_updated_template,
                        TimeUtils.getFlashcardSetTime(result.getModifiedDate())));
                imageIcon.setVisibility(result.hasImages() ? View.VISIBLE : View.GONE);
                imageText.setVisibility(result.hasImages() ? View.VISIBLE : View.GONE);

                boolean setAlreadyInLibrary = databaseManager.alreadyHasQuizletSet(result.getQuizletSetId());
                inLibraryText.setVisibility(setAlreadyInLibrary ? View.VISIBLE : View.GONE);

                sideInfo.setVisibility(result.hasImages() || setAlreadyInLibrary ? View.VISIBLE : View.GONE);

                adjustForDarkMode();
            }
        }

        void adjustForDarkMode() {
            card.setProperColors();
            title.setProperColors();
            numFlashcardsText.setProperColors();
            createdOn.setProperColors();
            lastUpdated.setProperColors();
            imageIcon.setProperColors();
            imageText.setProperTextColor();
        }

        @OnClick(R.id.parent)
        public void onCellClicked() {
            listener.onResultClicked(results.get(getAdapterPosition()));
        }
    }
}
