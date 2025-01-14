package com.randomappsinc.simpleflashcards.csvimport;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.activities.StandardActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.dialogs.ConfirmQuitDialog;
import com.randomappsinc.simpleflashcards.common.models.Flashcard;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.DeleteFlashcardDialog;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.EditFlashcardDefinitionDialog;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.EditFlashcardTermDialog;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
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
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public class CsvImportActivity extends StandardActivity implements EditFlashcardTermDialog.Listener,
        EditFlashcardDefinitionDialog.Listener, CsvFlashcardsAdapter.Listener,
        DeleteFlashcardDialog.Listener, ConfirmQuitDialog.Listener {

    @BindView(R.id.set_name_input) EditText setNameInput;
    @BindView(R.id.flashcards) RecyclerView flashcardsList;

    private CsvFlashcardsAdapter flashcardsAdapter;
    private EditFlashcardTermDialog termDialog;
    private EditFlashcardDefinitionDialog definitionDialog;
    private DeleteFlashcardDialog deleteFlashcardDialog;
    private ConfirmQuitDialog confirmQuitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csv_import);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        extractFileIntoFlashcardSet();

        termDialog = new EditFlashcardTermDialog(this, this);
        definitionDialog = new EditFlashcardDefinitionDialog(this, this);
        deleteFlashcardDialog = new DeleteFlashcardDialog(this, this);
        confirmQuitDialog = new ConfirmQuitDialog(
                this, this, R.string.confirm_csv_quit_body);
    }

    private void onFlashcardSetExtracted(String setName, List<Flashcard> flashcards) {
        runOnUiThread(() -> {
            setNameInput.setText(setName);
            flashcardsAdapter = new CsvFlashcardsAdapter(flashcards, this);
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
                CsvReader csvReader = new CsvReader();
                CsvParser csvParser = csvReader.parse(reader);

                CsvRow row;
                while ((row = csvParser.nextRow()) != null) {
                    String term = "";
                    try {
                        term = row.getField(0);
                    } catch (IndexOutOfBoundsException ignored) {}
                    String definition = "";
                    try {
                        definition = row.getField(1);
                    } catch (IndexOutOfBoundsException ignored) {}

                    Flashcard flashcard = new Flashcard(term, definition);
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

    @Override
    public void onEditTermRequested(Flashcard flashcard) {
        termDialog.show(flashcard.getTerm());
    }

    @Override
    public void onEditDefinitionRequested(Flashcard flashcard) {
        definitionDialog.show(flashcard.getDefinition());
    }

    @Override
    public void onDeleteFlashcardRequested() {
        deleteFlashcardDialog.show();
    }

    @Override
    public void onFlashcardTermEdited(String newTerm) {
        flashcardsAdapter.onTermEdited(newTerm);
    }

    @Override
    public void onFlashcardDefinitionEdited(String newDefinition) {
        flashcardsAdapter.onDefinitionEdited(newDefinition);
    }

    @Override
    public void onFlashcardDeleted() {
        flashcardsAdapter.onFlashcardDeleted();
    }

    @OnClick(R.id.save)
    public void save() {
        String setName = setNameInput.getText().toString().trim();
        if (setName.isEmpty()) {
            UIUtils.showLongToast(R.string.empty_set_name, this);
            return;
        }
        DatabaseManager databaseManager = DatabaseManager.get();
        databaseManager.addFlashcardSet(setName, flashcardsAdapter.getFlashcards());
        UIUtils.showShortToast(R.string.flashcard_set_saved, this);
        finish();
    }

    @Override
    public void onQuitConfirmed() {
        finish();
    }

    @Override
    public void onBackPressed() {
        confirmQuitDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        termDialog.cleanUp();
        definitionDialog.cleanUp();
        deleteFlashcardDialog.cleanUp();
        confirmQuitDialog.cleanUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            confirmQuitDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
