package com.randomappsinc.simpleflashcards.csvimport;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.models.Flashcard;
import com.randomappsinc.simpleflashcards.theme.ThemedTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CsvFlashcardsAdapter extends RecyclerView.Adapter<CsvFlashcardsAdapter.FlashcardViewHolder> {

    protected List<Flashcard> flashcardList;

    public CsvFlashcardsAdapter(List<Flashcard> flashcardList) {
        this.flashcardList = flashcardList;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.csv_import_flashcard_cell,
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
        return flashcardList.size();
    }

    public class FlashcardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.flashcard_position) TextView positionText;
        @BindView(R.id.term_text) ThemedTextView termText;
        @BindView(R.id.definition_text) ThemedTextView definitionText;

        FlashcardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFlashcard(int position) {
            Flashcard flashcard = flashcardList.get(position);
            Context context = positionText.getContext();
            positionText.setText(context.getString(
                    R.string.x_of_y,
                    getAdapterPosition() + 1,
                    flashcardList.size()));

            String term = flashcard.getTerm();
            if (TextUtils.isEmpty(term)) {
                termText.setTextAsHint(R.string.no_term_hint);
            } else {
                termText.setTextNormally(term);
            }

            String definition = flashcard.getDefinition();
            if (TextUtils.isEmpty(definition)) {
                definitionText.setTextAsHint(R.string.no_definition_hint);
            } else {
                definitionText.setTextNormally(definition);
            }
        }
    }
}
