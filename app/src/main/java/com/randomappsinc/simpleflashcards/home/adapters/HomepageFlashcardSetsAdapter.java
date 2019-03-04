package com.randomappsinc.simpleflashcards.home.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomepageFlashcardSetsAdapter extends RecyclerView.Adapter<HomepageFlashcardSetsAdapter.FlashcardSetViewHolder> {

    public interface Listener {
        void browseFlashcardSet(FlashcardSet flashcardSet);

        void takeQuiz(FlashcardSet flashcardSet);

        void editFlashcardSet(FlashcardSet flashcardSet);

        void deleteFlashcardSet(FlashcardSet flashcardSet);

        void onContentUpdated(int numSets);
    }

    @NonNull protected Listener listener;
    private Context context;
    protected List<FlashcardSet> flashcardSets;
    protected int selectedItemIndex = -1;

    public HomepageFlashcardSetsAdapter(@NonNull Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        this.flashcardSets = new ArrayList<>();
    }

    public void refreshContent(String searchTerm) {
        flashcardSets.clear();
        flashcardSets.addAll(DatabaseManager.get().getFlashcardSets(searchTerm));
        notifyDataSetChanged();
        listener.onContentUpdated(getItemCount());
    }

    public void onFlashcardSetDeleted() {
        if (selectedItemIndex < 0) {
            return;
        }
        flashcardSets.remove(selectedItemIndex);
        notifyItemRemoved(selectedItemIndex);
        selectedItemIndex = -1;
        listener.onContentUpdated(getItemCount());
    }

    @NonNull
    @Override
    public FlashcardSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(
                R.layout.flashcard_set_cell,
                parent,
                false);
        return new FlashcardSetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardSetViewHolder holder, int position) {
        holder.loadFlashcardSet(position);
    }

    @Override
    public int getItemCount() {
        return flashcardSets.size();
    }

    public class FlashcardSetViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.set_name) TextView setName;
        @BindView(R.id.num_flashcards) TextView numFlashcards;

        FlashcardSetViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFlashcardSet(int position) {
            FlashcardSet flashcardSet = flashcardSets.get(position);
            setName.setText(flashcardSet.getName());
            numFlashcards.setText(String.valueOf(flashcardSet.getFlashcards().size()));
        }

        @OnClick(R.id.browse_button)
        public void browseFlashcards() {
            listener.browseFlashcardSet(flashcardSets.get(getAdapterPosition()));
        }

        @OnClick(R.id.quiz_button)
        public void takeQuiz() {
            listener.takeQuiz(flashcardSets.get(getAdapterPosition()));
        }

        @OnClick(R.id.edit_button)
        public void editFlashcardSet() {
            listener.editFlashcardSet(flashcardSets.get(getAdapterPosition()));
        }

        @OnClick(R.id.delete_button)
        public void deleteFlashcardSet() {
            selectedItemIndex = getAdapterPosition();
            listener.deleteFlashcardSet(flashcardSets.get(getAdapterPosition()));
        }
    }
}
