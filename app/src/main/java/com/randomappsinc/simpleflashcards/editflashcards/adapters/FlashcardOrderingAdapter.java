package com.randomappsinc.simpleflashcards.editflashcards.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.theme.ThemedTextView;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/** Adapter for rendering a list of flashcard sets the user can drag and drop around */
public class FlashcardOrderingAdapter
        extends RecyclerView.Adapter<FlashcardOrderingAdapter.FlashcardViewHolder>
        implements ItemTouchHelperAdapter {

    protected List<FlashcardDO> flashcards;

    public FlashcardOrderingAdapter(List<FlashcardDO> flashcards) {
        this.flashcards = flashcards;
    }

    public List<FlashcardDO> getFlashcards() {
        return flashcards;
    }

    public void sortFlashcards(Comparator<FlashcardDO> comparator) {
        Collections.sort(flashcards, comparator);
        notifyDataSetChanged();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        FlashcardDO movedCard = flashcards.remove(fromPosition);
        flashcards.add(toPosition, movedCard);
        notifyItemMoved(fromPosition, toPosition);
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.movable_flashcard,
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

    public class FlashcardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.term_text) ThemedTextView termText;
        @BindView(R.id.term_image) ImageView termImage;
        @BindView(R.id.definition_text) ThemedTextView definitionText;
        @BindView(R.id.definition_image) ImageView definitionImage;

        FlashcardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFlashcard(int position) {
            FlashcardDO flashcard = flashcards.get(position);

            String term = flashcard.getTerm();
            if (TextUtils.isEmpty(term)) {
                termText.setTextAsHint(R.string.no_term);
            } else {
                termText.setTextNormally(term);
            }

            String termImageUrl = flashcard.getTermImageUrl();
            if (TextUtils.isEmpty(termImageUrl)) {
                termImage.setVisibility(View.GONE);
            } else {
                termImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(termImageUrl)
                        .fit()
                        .centerCrop()
                        .into(termImage);
            }

            String definition = flashcard.getDefinition();
            if (TextUtils.isEmpty(definition)) {
                definitionText.setTextAsHint(R.string.no_description);
            } else {
                definitionText.setTextNormally(definition);
            }

            String definitionImageUrl = flashcard.getDefinitionImageUrl();
            if (TextUtils.isEmpty(definitionImageUrl)) {
                definitionImage.setVisibility(View.GONE);
            } else {
                definitionImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(definitionImageUrl)
                        .fit()
                        .centerCrop()
                        .into(definitionImage);
            }
        }
    }
}
