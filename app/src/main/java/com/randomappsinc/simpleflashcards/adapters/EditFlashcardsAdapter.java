package com.randomappsinc.simpleflashcards.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.Flashcard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditFlashcardsAdapter extends RecyclerView.Adapter<EditFlashcardsAdapter.FlashcardViewHolder> {

    public interface Listener {
        void onEditTerm(Flashcard flashcard);

        void onEditDefinition(Flashcard flashcard);

        void onDeleteFlashcard(Flashcard flashcard);

        void onImageClicked(Flashcard flashcard, boolean forTerm);

        void onAddImageClicked(Flashcard flashcard, boolean forTerm);
    }

    protected Listener listener;
    protected List<Flashcard> flashcards;
    protected List<Flashcard> filteredFlashcards;
    private TextView noContent;
    private int setId;
    private TextView numFlashcards;
    protected int selectedItemPosition = -1;
    private String currentQuery = "";

    public EditFlashcardsAdapter(Listener listener, int setId, TextView noContent, TextView numFlashcards) {
        this.listener = listener;
        this.filteredFlashcards = new ArrayList<>();
        this.setId = setId;
        this.noContent = noContent;
        this.numFlashcards = numFlashcards;
        refreshSet();
    }

    public void setCurrentQuery(String query) {
        currentQuery = query;
        filterFlashcards();
    }

    public void filterFlashcards() {
        filteredFlashcards.clear();
        if (currentQuery.isEmpty()) {
            filteredFlashcards.addAll(flashcards);
        } else {
            String lowerCaseQuery = currentQuery.toLowerCase();
            for (Flashcard flashcard : flashcards) {
                if (flashcard.getTerm().toLowerCase().contains(lowerCaseQuery)
                        || flashcard.getDefinition().toLowerCase().contains(lowerCaseQuery)) {
                    filteredFlashcards.add(flashcard);
                }
            }
        }
        notifyDataSetChanged();
        setNoContent();
        refreshCount();
    }

    public void onFlashcardDeleted() {
        refreshSet();
    }

    public void onFlashcardTermEdited(String newTerm) {
        if (selectedItemPosition < 0) {
            return;
        }
        filteredFlashcards.get(selectedItemPosition).setTerm(newTerm);
        notifyItemChanged(selectedItemPosition);
        selectedItemPosition = -1;
    }

    public void onFlashcardDefinitionEdited(String newDefinition) {
        if (selectedItemPosition < 0) {
            return;
        }
        filteredFlashcards.get(selectedItemPosition).setDefinition(newDefinition);
        notifyItemChanged(selectedItemPosition);
        selectedItemPosition = -1;
    }

    public void onTermImageUpdated(@Nullable String imageUrl) {
        if (selectedItemPosition < 0) {
            return;
        }
        filteredFlashcards.get(selectedItemPosition).setTermImageUrl(imageUrl);
        notifyDataSetChanged();
        selectedItemPosition = -1;
    }

    public void onDefinitionImageUpdated(@Nullable String imageUrl) {
        if (selectedItemPosition < 0) {
            return;
        }
        filteredFlashcards.get(selectedItemPosition).setDefinitionImageUrl(imageUrl);
        notifyDataSetChanged();
        selectedItemPosition = -1;
    }

    private void setNoContent() {
        if (flashcards.isEmpty()) {
            noContent.setText(R.string.no_flashcards_with_add_cta);
            noContent.setVisibility(View.VISIBLE);
        } else if (filteredFlashcards.isEmpty()) {
            noContent.setText(R.string.no_flashcards_found_in_search);
            noContent.setVisibility(View.VISIBLE);
        } else {
            noContent.setVisibility(View.GONE);
        }
    }

    public void refreshSet() {
        this.flashcards = DatabaseManager.get().getAllFlashcards(setId);
        filterFlashcards();
    }

    protected void refreshCount() {
        int flashcardsCount = getItemCount();
        Context context = numFlashcards.getContext();
        String numFlashcardsText = flashcardsCount == 1
                ? context.getString(R.string.one_flashcard)
                : context.getString(R.string.x_flashcards, flashcardsCount);
        numFlashcards.setText(numFlashcardsText);
    }

    public Flashcard getCurrentlyChosenFlashcard() {
        return filteredFlashcards.get(selectedItemPosition);
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.edit_flashcard_cell,
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
        return filteredFlashcards.size();
    }

    public class FlashcardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.position_info) TextView positionInfo;
        @BindView(R.id.term_text) TextView termText;
        @BindView(R.id.term_image) ImageView termImage;
        @BindView(R.id.add_term_image) View addImage;
        @BindView(R.id.definition) TextView definition;

        FlashcardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFlashcard(int position) {
            Flashcard flashcard = filteredFlashcards.get(position);
            positionInfo.setText(positionInfo.getContext().getString(
                    R.string.flashcard_x_of_y,
                    position + 1,
                    getItemCount()));
            termText.setText(flashcard.getTerm());
            String imageUrl = flashcard.getTermImageUrl();
            if (imageUrl == null) {
                termImage.setVisibility(View.GONE);
                addImage.setVisibility(View.VISIBLE);
            } else {
                addImage.setVisibility(View.GONE);
                termImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(imageUrl)
                        .fit()
                        .centerCrop()
                        .into(termImage);
            }
            definition.setText(flashcard.getDefinition());
        }

        @OnClick(R.id.term_text)
        public void editTerm() {
            selectedItemPosition = getAdapterPosition();
            listener.onEditTerm(filteredFlashcards.get(getAdapterPosition()));
        }

        @OnClick(R.id.add_term_image)
        public void addTermImage() {
            selectedItemPosition = getAdapterPosition();
            listener.onAddImageClicked(filteredFlashcards.get(getAdapterPosition()), true);
        }

        @OnClick(R.id.term_image)
        public void onTermImageClicked() {
            selectedItemPosition = getAdapterPosition();
            listener.onImageClicked(filteredFlashcards.get(getAdapterPosition()), true);
        }

        @OnClick(R.id.definition)
        public void editDefinition() {
            selectedItemPosition = getAdapterPosition();
            listener.onEditDefinition(filteredFlashcards.get(getAdapterPosition()));
        }

        @OnClick(R.id.delete_flashcard)
        public void deleteFlashcard() {
            listener.onDeleteFlashcard(filteredFlashcards.get(getAdapterPosition()));
        }
    }
}
