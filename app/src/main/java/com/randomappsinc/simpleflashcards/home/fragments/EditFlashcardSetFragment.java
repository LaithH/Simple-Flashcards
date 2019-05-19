package com.randomappsinc.simpleflashcards.home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.constants.Language;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.editflashcards.activities.EditFlashcardsActivity;
import com.randomappsinc.simpleflashcards.editflashcards.activities.PickAndImportFlashcardsActivity;
import com.randomappsinc.simpleflashcards.editflashcards.activities.ReorderFlashcardsActivity;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.EditFlashcardSetNameDialog;
import com.randomappsinc.simpleflashcards.editflashcards.dialogs.SetLanguagesDialog;
import com.randomappsinc.simpleflashcards.editflashcards.managers.ImportFlashcardsManager;
import com.randomappsinc.simpleflashcards.home.activities.FlashcardSetActivity;
import com.randomappsinc.simpleflashcards.home.adapters.FlashcardSetOptionsAdapter;
import com.randomappsinc.simpleflashcards.home.dialogs.DeleteFlashcardSetDialog;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.persistence.models.FlashcardSetDO;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditFlashcardSetFragment extends Fragment
        implements FlashcardSetOptionsAdapter.ItemSelectionListener, DeleteFlashcardSetDialog.Listener,
        ImportFlashcardsManager.Listener, SetLanguagesDialog.Listener, EditFlashcardSetNameDialog.Listener {

    public static EditFlashcardSetFragment getInstance(int setId) {
        EditFlashcardSetFragment fragment = new EditFlashcardSetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.FLASHCARD_SET_ID_KEY, setId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.edit_set_options) RecyclerView editSetOptions;

    private int setId;
    private ImportFlashcardsManager importFlashcardsManager;
    private SetLanguagesDialog setLanguagesDialog;
    private EditFlashcardSetNameDialog editFlashcardSetNameDialog;
    private DeleteFlashcardSetDialog deleteFlashcardSetDialog;
    private DatabaseManager databaseManager = DatabaseManager.get();
    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.edit_flashcard_set_fragment,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setId = getArguments().getInt(Constants.FLASHCARD_SET_ID_KEY);
        editSetOptions.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        editSetOptions.setAdapter(new FlashcardSetOptionsAdapter(
                getActivity(),
                this,
                R.array.flashcard_set_edit_options,
                R.array.flashcard_set_edit_icons));
        importFlashcardsManager = new ImportFlashcardsManager(getActivity(), this, setId);
        FlashcardSetDO flashcardSetDO = databaseManager.getFlashcardSet(setId);
        setLanguagesDialog = new SetLanguagesDialog(getActivity(), flashcardSetDO, this);
        editFlashcardSetNameDialog = new EditFlashcardSetNameDialog(
                getActivity(),
                flashcardSetDO.getName(),
                this);
        deleteFlashcardSetDialog = new DeleteFlashcardSetDialog(getActivity(), this);
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(
                        getActivity(), EditFlashcardsActivity.class)
                        .putExtra(Constants.FLASHCARD_SET_ID_KEY, setId));
                getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
                break;
            case 1:
                importFlashcardsManager.startImport();
                break;
            case 2:
                setLanguagesDialog.show();
                break;
            case 3:
                startActivity(new Intent(
                        getActivity(), ReorderFlashcardsActivity.class)
                        .putExtra(Constants.FLASHCARD_SET_ID_KEY, setId));
                getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
                break;
            case 4:
                editFlashcardSetNameDialog.show();
                break;
            case 5:
                FlashcardSetDO flashcardSetDO = databaseManager.getFlashcardSet(setId);
                deleteFlashcardSetDialog.show(flashcardSetDO);
                break;
        }
    }

    @Override
    public void onSetChosen(FlashcardSetDO flashcardSet, int importMode) {
        Intent intent = new Intent(getActivity(), PickAndImportFlashcardsActivity.class)
                .putExtra(Constants.RECEIVING_SET_ID, setId)
                .putExtra(Constants.SENDING_SET_ID, flashcardSet.getId())
                .putExtra(Constants.IMPORT_MODE_KEY, importMode);
        getActivity().startActivityForResult(intent, FlashcardSetActivity.IMPORT_CODE);
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
    }

    @Override
    public void onLanguagesSelected(@Language int termsLanguage, @Language int definitionsLanguage) {
        databaseManager.updateSetLanguages(setId, termsLanguage, definitionsLanguage);
    }

    @Override
    public void onFlashcardSetRename(String newSetName) {
        databaseManager.renameSet(setId, newSetName);
        ((FlashcardSetActivity) getActivity()).refreshView();
        UIUtils.showShortToast(R.string.flashcard_set_renamed, getContext());
    }

    @Override
    public void onFlashcardSetDeleted(int flashcardSetId) {
        databaseManager.deleteFlashcardSet(flashcardSetId);
        UIUtils.showShortToast(R.string.flashcard_set_deleted, getContext());
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
