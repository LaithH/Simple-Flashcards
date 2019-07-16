package com.randomappsinc.simpleflashcards.quizlet.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.quizlet.api.models.QuizletFlashcard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuizletFlashcardsAdapter
        extends RecyclerView.Adapter<QuizletFlashcardsAdapter.FlashcardViewHolder> {

    public interface Listener {
        void onImageClicked(QuizletFlashcard flashcard);
    }

    @NonNull protected Listener listener;
    protected List<QuizletFlashcard> flashcards = new ArrayList<>();

    public QuizletFlashcardsAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void loadFlashcards(List<QuizletFlashcard> flashcards) {
        this.flashcards.clear();
        this.flashcards.addAll(flashcards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.quizlet_flashcard_cell,
                parent,
                false);
        return new FlashcardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        holder.loadFlashcard(position);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    class FlashcardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.position_info) TextView positionInfo;
        @BindView(R.id.term_text) TextView termText;
        @BindView(R.id.term_image) ImageView termImage;
        @BindView(R.id.definition) TextView definition;

        FlashcardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFlashcard(int position) {
            QuizletFlashcard flashcard = flashcards.get(position);
            Context context = positionInfo.getContext();
            positionInfo.setText(context.getString(
                    R.string.flashcard_x_of_y,
                    position + 1,
                    getItemCount()));
            termText.setText(flashcard.getTerm());
            String imageUrl = flashcard.getImageUrl();
            if (imageUrl == null) {
                termImage.setVisibility(View.GONE);
            } else {
                termImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(imageUrl)
                        .fit()
                        .centerCrop()
                        .into(termImage);
            }
            definition.setText(flashcard.getDefinition());
        }

        @OnClick(R.id.term_image)
        public void openImageInFullView() {
            QuizletFlashcard flashcard = flashcards.get(getAdapterPosition());
            if (!TextUtils.isEmpty(flashcard.getImageUrl())) {
                listener.onImageClicked(flashcard);
            }
        }
    }
}
