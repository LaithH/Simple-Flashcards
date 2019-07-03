package com.randomappsinc.simpleflashcards.home.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.home.adapters.FlashcardSetOptionsAdapter;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardDO;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.utils.FileUtils;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;

public class FlashcardSetMoreOptionsFragment extends Fragment
        implements FlashcardSetOptionsAdapter.ItemSelectionListener {

    public static FlashcardSetMoreOptionsFragment getInstance(int setId) {
        FlashcardSetMoreOptionsFragment fragment = new FlashcardSetMoreOptionsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.FLASHCARD_SET_ID_KEY, setId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.flashcard_set_more_options) RecyclerView moreOptionsList;

    private int setId;
    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.flashcard_set_more_options,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setId = getArguments().getInt(Constants.FLASHCARD_SET_ID_KEY);
        moreOptionsList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        moreOptionsList.setAdapter(new FlashcardSetOptionsAdapter(
                getActivity(),
                this,
                R.array.flashcard_set_more_options,
                R.array.flashcard_set_more_options_icons));
    }

    @Override
    public void onItemClick(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            FlashcardSetDO flashcardSetDO = DatabaseManager.get().getFlashcardSet(setId);
            File csvFile = FileUtils.createCsvFileForSet(getContext(), flashcardSetDO);
            if (csvFile == null) {
                UIUtils.showLongToast(R.string.export_csv_failed, getContext());
                return;
            }

            try {
                CsvWriter csvWriter = new CsvWriter();
                FileWriter fileWriter = new FileWriter(csvFile, true);
                CsvAppender csvAppender = csvWriter.append(fileWriter);
                for (FlashcardDO flashcardDO : flashcardSetDO.getFlashcards()) {
                    csvAppender.appendLine(flashcardDO.getTerm(), flashcardDO.getDefinition());
                }
                csvAppender.flush();
            } catch (IOException exception) {
                UIUtils.showLongToast(R.string.export_csv_failed, getContext());
                return;
            }

            Uri fileUri = FileProvider.getUriForFile(getContext(),
                    "com.randomappsinc.simpleflashcards.provider",
                    csvFile);
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.setType("application/csv");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri);
            intentShareFile.putExtra(
                    Intent.EXTRA_SUBJECT, getString(R.string.export_csv_title, flashcardSetDO.getName()));
            startActivity(Intent.createChooser(intentShareFile, getString(R.string.export_csv_with)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
