package com.randomappsinc.simpleflashcards.ocr;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** Adapter for rendering a list of the text blocks recognized by OCR */
public class OcrTextSelectionAdapter extends RecyclerView.Adapter<OcrTextSelectionAdapter.TextBlockViewHolder> {

    public interface Listener {
        void onNumSelectedTextBlocksUpdated(int numSelectedSets);
    }

    protected List<String> textBlocks = new ArrayList<>();
    protected List<String> selectedTextBlocks = new ArrayList<>();
    protected Listener listener;

    public OcrTextSelectionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setTextBlocks(List<String> newTextBlocks) {
        textBlocks.clear();
        textBlocks.addAll(newTextBlocks);
        selectedTextBlocks.clear();
        notifyDataSetChanged();
        listener.onNumSelectedTextBlocksUpdated(selectedTextBlocks.size());
    }

    public String getSelectedText() {
        return TextUtils.join("\n", selectedTextBlocks);
    }

    @NonNull
    @Override
    public OcrTextSelectionAdapter.TextBlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.text_block_selection_cell,
                parent,
                false);
        return new TextBlockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OcrTextSelectionAdapter.TextBlockViewHolder holder, int position) {
        holder.loadTextBlock(position);
    }

    @Override
    public int getItemCount() {
        return textBlocks.size();
    }

    public class TextBlockViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_block)
        TextView textBlockTextView;
        @BindView(R.id.set_selected_toggle)
        CheckBox setSelectedToggle;

        TextBlockViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadTextBlock(int position) {
            String textBlock = textBlocks.get(position);
            textBlockTextView.setText(textBlock);
            setSelectedToggle.setChecked(selectedTextBlocks.contains(textBlock));
        }

        @OnClick(R.id.text_block_for_selection_parent)
        public void onTextBlockCellClicked() {
            String textBlock = textBlocks.get(getAdapterPosition());
            if (selectedTextBlocks.contains(textBlock)) {
                selectedTextBlocks.remove(textBlock);
                setSelectedToggle.setChecked(false);
            } else {
                selectedTextBlocks.add(textBlock);
                setSelectedToggle.setChecked(true);
            }
            listener.onNumSelectedTextBlocksUpdated(selectedTextBlocks.size());
        }

        @OnClick(R.id.set_selected_toggle)
        public void onTextBlockSelection() {
            String flashcardSet = textBlocks.get(getAdapterPosition());
            if (selectedTextBlocks.contains(flashcardSet)) {
                selectedTextBlocks.remove(flashcardSet);
            } else {
                selectedTextBlocks.add(flashcardSet);
            }
            listener.onNumSelectedTextBlocksUpdated(selectedTextBlocks.size());
        }
    }
}
