package com.randomappsinc.simpleflashcards.home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.home.adapters.FlashcardSetOptionsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
