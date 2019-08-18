package com.randomappsinc.simpleflashcards.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;

/** This manager handles the recognition of text from an image */
public class TextRecognitionManager {

    public interface Listener {
        void onTextBlocksRecognized(List<String> textBlocks);

        void onTextRecognitionFailed();
    }

    private Listener listener;
    private TextRecognizer textRecognizer;

    public TextRecognitionManager(Context context, Listener listener) {
        this.listener = listener;
        this.textRecognizer = new TextRecognizer.Builder(context).build();
    }

    public void analyzeImage(Bitmap bitmap) {
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        if (!textRecognizer.isOperational()) {
            listener.onTextRecognitionFailed();
            return;
        }
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);
        List<String> textList = new ArrayList<>();
        for (int i = 0; i < textBlocks.size(); i++) {
            int key = textBlocks.keyAt(i);
            TextBlock textBlock = textBlocks.get(key);
            textList.add(textBlock.getValue());
        }
        listener.onTextBlocksRecognized(textList);
    }

    public void cleanUp() {
        listener = null;
        textRecognizer.release();
    }
}
