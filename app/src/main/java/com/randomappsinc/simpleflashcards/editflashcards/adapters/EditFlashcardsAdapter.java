package com.randomappsinc.simpleflashcards.editflashcards.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.theme.ThemedLearnedToggle;
import com.randomappsinc.simpleflashcards.theme.ThemedTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditFlashcardsAdapter extends RecyclerView.Adapter<EditFlashcardsAdapter.FlashcardViewHolder> {

    public interface Listener {
        void onLearnedStatusChanged(FlashcardDO flashcard, boolean learned);

        void onEditTerm(FlashcardDO flashcard);

        void onEditDefinition(FlashcardDO flashcard);

        void onDeleteFlashcard(FlashcardDO flashcard);

        void onImageClicked(FlashcardDO flashcard, boolean forTerm);

        void onAddImageClicked(FlashcardDO flashcard, boolean forTerm);
    }

    protected Listener listener;
    protected List<FlashcardDO> flashcards;
    protected List<FlashcardDO> filteredFlashcards;
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
            for (FlashcardDO flashcard : flashcards) {
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

    public void setAllLearnedStatuses(boolean learned) {
        for (FlashcardDO flashcardDO : flashcards) {
            flashcardDO.setLearned(learned);
        }
        notifyDataSetChanged();
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

    public FlashcardDO getCurrentlyChosenFlashcard() {
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

        @BindView(R.id.learned_toggle) ThemedLearnedToggle learnedToggle;
        @BindView(R.id.term_text) ThemedTextView termText;
        @BindView(R.id.term_image) ImageView termImage;
        @BindView(R.id.add_term_image) View addTermImage;
        @BindView(R.id.definition_text) ThemedTextView definitionText;
        @BindView(R.id.definition_image) ImageView definitionImage;
        @BindView(R.id.add_definition_image) View addDefinitionImage;

        FlashcardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFlashcard(int position) {
            FlashcardDO flashcard = filteredFlashcards.get(position);

            learnedToggle.setLearned(flashcard.isLearned());

            String term = flashcard.getTerm();
            if (TextUtils.isEmpty(term)) {
                termText.setTextAsHint(R.string.no_term_hint);
            } else {
                termText.setTextNormally(term);
            }

            String termImageUrl = flashcard.getTermImageUrl();
            if (termImageUrl == null) {
                termImage.setVisibility(View.GONE);
                addTermImage.setVisibility(View.VISIBLE);
            } else {
                addTermImage.setVisibility(View.GONE);
                termImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(termImageUrl)
                        .fit()
                        .centerCrop()
                        .into(termImage);
            }

            String definition = flashcard.getDefinition();
            if (TextUtils.isEmpty(definition)) {
                definitionText.setTextAsHint(R.string.no_definition_hint);
            } else {
                definitionText.setTextNormally(definition);
            }

            String definitionImageUrl = flashcard.getDefinitionImageUrl();
            if (definitionImageUrl == null) {
                definitionImage.setVisibility(View.GONE);
                addDefinitionImage.setVisibility(View.VISIBLE);
            } else {
                addDefinitionImage.setVisibility(View.GONE);
                definitionImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(definitionImageUrl)
                        .fit()
                        .centerCrop()
                        .into(definitionImage);
            }
        }

        @OnClick(R.id.learned_toggle)
        public void toggleLearnedStatus() {
            FlashcardDO flashcard = filteredFlashcards.get(getAdapterPosition());
            boolean newLearnedStatus = !flashcard.isLearned();
            learnedToggle.setLearned(newLearnedStatus);
            flashcard.setLearned(newLearnedStatus);
            listener.onLearnedStatusChanged(flashcard, newLearnedStatus);
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

        @OnClick(R.id.definition_text)
        public void editDefinition() {
            selectedItemPosition = getAdapterPosition();
            listener.onEditDefinition(filteredFlashcards.get(getAdapterPosition()));
        }

        @OnClick(R.id.add_definition_image)
        public void addDefinitionImage() {
            selectedItemPosition = getAdapterPosition();
            listener.onAddImageClicked(filteredFlashcards.get(getAdapterPosition()), false);
        }

        @OnClick(R.id.definition_image)
        public void onDefinitionImageClicked() {
            selectedItemPosition = getAdapterPosition();
            listener.onImageClicked(filteredFlashcards.get(getAdapterPosition()), false);
        }

        @OnClick(R.id.delete_flashcard)
        public void deleteFlashcard() {
            listener.onDeleteFlashcard(filteredFlashcards.get(getAdapterPosition()));
        }
    }
}
