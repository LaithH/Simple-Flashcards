package com.randomappsinc.simpleflashcards.browse.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.browse.activities.BrowseFlashcardsActivity;
import com.randomappsinc.simpleflashcards.browse.managers.BrowseFlashcardsSettingsManager;
import com.randomappsinc.simpleflashcards.common.activities.PictureFullViewActivity;
import com.randomappsinc.simpleflashcards.common.constants.Constants;
import com.randomappsinc.simpleflashcards.common.models.Flashcard;
import com.randomappsinc.simpleflashcards.persistence.DatabaseManager;
import com.randomappsinc.simpleflashcards.theme.ThemedLearnedToggle;
import com.randomappsinc.simpleflashcards.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BrowseFlashcardFragment extends Fragment {

    // I do not know how this works.
    private static final int CAMERA_DISTANCE = 8000;

    public static BrowseFlashcardFragment create(Flashcard flashcard) {
        BrowseFlashcardFragment flashcardFragment = new BrowseFlashcardFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.FLASHCARD_KEY, flashcard);
        flashcardFragment.setArguments(bundle);
        return flashcardFragment;
    }

    @BindView(R.id.flashcard_container) View flashcardContainer;
    @BindView(R.id.learned_toggle) ThemedLearnedToggle learnedToggle;
    @BindView(R.id.speak) View speak;
    @BindView(R.id.flip_icon) View flipIcon;
    @BindView(R.id.content_container) ViewGroup contentContainer;

    protected ImageView cardImage;
    protected TextView content;

    @BindInt(R.integer.default_anim_length) int flipAnimLength;

    protected Flashcard flashcard;
    protected boolean isShowingTerm;
    private DatabaseManager databaseManager = DatabaseManager.get();
    protected BrowseFlashcardsSettingsManager settingsManager = BrowseFlashcardsSettingsManager.get();
    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.flashcard_for_browsing,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);

        settingsManager.addDefaultSideListener(flashcardListener);
        isShowingTerm = settingsManager.getShowTermsByDefault();

        Bundle bundle = getArguments();
        Preconditions.checkNotNull(bundle, "No arguments passed to flashcard browse fragment.");
        flashcard = getArguments().getParcelable(Constants.FLASHCARD_KEY);
        Preconditions.checkNotNull(flashcard, "No flashcard passed to flashcard browse fragment.");
        learnedToggle.setLearned(flashcard.isLearned());

        float scale = getResources().getDisplayMetrics().density * CAMERA_DISTANCE;
        flashcardContainer.setCameraDistance(scale);

        loadFlashcardIntoView();

        return rootView;
    }

    @OnClick(R.id.learned_toggle)
    public void toggleLearnedStatus() {
        boolean newLearnedStatus = !flashcard.isLearned();
        learnedToggle.setLearned(newLearnedStatus);
        flashcard.setLearned(newLearnedStatus);
        databaseManager.setLearnedStatus(flashcard.getId(), newLearnedStatus);

        BrowseFlashcardsActivity activity = (BrowseFlashcardsActivity) getActivity();
        if (newLearnedStatus && activity != null) {
            activity.onFlashcardLearned();
        }
    }

    @OnClick(R.id.flashcard_container)
    public void flipFlashcard() {
        stopSpeaking();
        flashcardContainer.setEnabled(false);
        flashcardContainer.clearAnimation();
        flashcardContainer
                .animate()
                .rotationY(180)
                .setDuration(flipAnimLength)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isShowingTerm = !isShowingTerm;
                        learnedToggle.setVisibility(View.GONE);
                        speak.setVisibility(View.GONE);
                        content.setVisibility(View.GONE);
                        cardImage.setVisibility(View.GONE);
                        flipIcon.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        flashcardContainer.setRotationY(0);
                        learnedToggle.setVisibility(View.VISIBLE);
                        loadFlashcardIntoView();
                        speak.setVisibility(View.VISIBLE);
                        flipIcon.setVisibility(View.VISIBLE);
                        flashcardContainer.setEnabled(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        flashcardContainer.setEnabled(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    protected void loadFlashcardIntoView() {
        // Load proper layout for card content based on orientation
        contentContainer.removeAllViews();
        int orientation = getActivity().getResources().getConfiguration().orientation;
        View contentView;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            contentView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.browse_flashcard_content_horizontal, contentContainer);
        } else {
            contentView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.browse_flashcard_content_vertical, contentContainer);
        }
        content = contentView.findViewById(R.id.content);
        content.setMovementMethod(new ScrollingMovementMethod());
        cardImage = contentView.findViewById(R.id.card_image);
        cardImage.setOnClickListener(imageClickListener);

        setUpText();
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, settingsManager.getTextSize());

        int textColor = settingsManager.getTextColor();
        if (textColor != Constants.UNSET_COLOR) {
            content.setTextColor(settingsManager.getTextColor());
        }

        setUpImageView();
    }

    protected void setUpText() {
        String contentText = isShowingTerm ? flashcard.getTerm() : flashcard.getDefinition();
        if (TextUtils.isEmpty(contentText)) {
            content.setVisibility(View.GONE);
        } else {
            content.setText(contentText);
            content.setVisibility(View.VISIBLE);
        }
    }

    protected void setUpImageView() {
        final String imageUrl = isShowingTerm ? flashcard.getTermImageUrl() : flashcard.getDefinitionImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            cardImage.setVisibility(View.VISIBLE);
            if (ViewCompat.isLaidOut(cardImage)) {
                loadImage(imageUrl);
            } else {
                ViewUtils.runOnPreDraw(cardImage, () -> loadImage(imageUrl));
            }
        } else {
            cardImage.setVisibility(View.GONE);
        }
    }

    protected void loadImage(String imageUrl) {
        if (cardImage != null && cardImage.getHeight() > 0) {
            Picasso.get()
                    .load(imageUrl)
                    .resize(0, cardImage.getHeight())
                    .into(cardImage);
        }
    }

    private final BrowseFlashcardsSettingsManager.FlashcardListener flashcardListener =
            new BrowseFlashcardsSettingsManager.FlashcardListener() {
                @Override
                public void onDefaultSideChanged(boolean showTermsByDefault) {
                    isShowingTerm = showTermsByDefault;
                    loadFlashcardIntoView();
                }

                @Override
                public void onTextSizeChanged(int textSize) {
                    content.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                }

                @Override
                public void onTextColorChanged(int textColor) {
                    content.setTextColor(textColor);
                }

                @Override
                public void refresh() {
                    isShowingTerm = settingsManager.getShowTermsByDefault();
                    setUpText();
                    setUpImageView();
                }
            };

    @OnClick(R.id.speak)
    public void speakFlashcard() {
        speak(isShowingTerm ? flashcard.getTerm() : flashcard.getDefinition());
    }

    private void speak(String text) {
        BrowseFlashcardsActivity activity = (BrowseFlashcardsActivity) getActivity();
        if (activity != null) {
            activity.speak(text, isShowingTerm);
        }
    }

    private void stopSpeaking() {
        BrowseFlashcardsActivity activity = (BrowseFlashcardsActivity) getActivity();
        if (activity != null) {
            activity.stopSpeaking();
        }
    }

    private final View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String imageUrl = isShowingTerm
                    ? flashcard.getTermImageUrl()
                    : flashcard.getDefinitionImageUrl();
            Activity activity = getActivity();
            if (!TextUtils.isEmpty(imageUrl) && activity != null) {
                Intent intent = new Intent(activity, PictureFullViewActivity.class)
                        .putExtra(Constants.IMAGE_URL_KEY, imageUrl)
                        .putExtra(Constants.CAPTION_KEY, isShowingTerm
                                ? flashcard.getTerm()
                                : flashcard.getDefinition());
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, 0);
            }
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadFlashcardIntoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        settingsManager.removeDefaultSideListener(flashcardListener);
        unbinder.unbind();
    }
}
