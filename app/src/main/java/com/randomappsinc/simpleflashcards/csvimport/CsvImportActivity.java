package com.randomappsinc.simpleflashcards.csvimport;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.models.Flashcard;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CsvImportActivity extends StandardActivity {

    @BindView(R.id.set_name_input) EditText setNameInput;
    @BindView(R.id.flashcards) RecyclerView flashcardsList;

    private CsvFlashcardsAdapter flashcardsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csv_import);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        extractFileIntoFlashcardSet();
    }

    private void onFlashcardSetExtracted(String setName, List<Flashcard> flashcards) {
        runOnUiThread(() -> {
            setNameInput.setText(setName);
            flashcardsAdapter = new CsvFlashcardsAdapter(flashcards);
            flashcardsList.setAdapter(flashcardsAdapter);
        });
    }

    private void extractFileIntoFlashcardSet() {
        Handler handler = new Handler();
        handler.post(() -> {
            Uri fileUri = Uri.parse(getIntent().getStringExtra(Constants.URI_KEY));
            Cursor cursor = getContentResolver().query(
                    fileUri,
                    null,
                    null,
                    null,
                    null,
                    null);

            String setName = "";
            if (cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                setName = displayName.replace(".csv", "");
                cursor.close();
            }

            List<Flashcard> flashcards = new ArrayList<>();
            InputStream inputStream = null;
            BufferedReader reader = null;
            try {
                inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    throw new IOException("Unable to find .csv file!");
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] termAndDefinition = line.split(",");
                    Flashcard flashcard = new Flashcard(termAndDefinition[0], termAndDefinition[1]);
                    flashcards.add(flashcard);
                }
            } catch (IOException exception) {
                UIUtils.showLongToast(R.string.csv_read_failed, this);
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ignored) {}
            }

            onFlashcardSetExtracted(setName, flashcards);
        });
    }

    @OnClick(R.id.save)
    public void save() {

    }
}
