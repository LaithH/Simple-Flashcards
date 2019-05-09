package com.randomappsinc.simpleflashcards.home.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.backupandrestore.activities.BackupAndRestoreActivity;
import com.randomappsinc.simpleflashcards.common.views.SimpleDividerItemDecoration;
import com.randomappsinc.simpleflashcards.home.adapters.SettingsAdapter;
import com.randomappsinc.simpleflashcards.nearbysharing.activities.NearbySharingActivity;
import com.randomappsinc.simpleflashcards.nearbysharing.managers.NearbyNameManager;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SettingsFragment extends Fragment implements SettingsAdapter.ItemSelectionListener {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public static final String SUPPORT_EMAIL = "RandomAppsInc61@gmail.com";
    public static final String OTHER_APPS_URL = "https://play.google.com/store/apps/dev?id=9093438553713389916";
    public static final String REPO_URL = "https://github.com/Gear61/Simple-Flashcards";

    @BindView(R.id.settings_options) RecyclerView settingsOptions;
    @BindString(R.string.feedback_subject) String feedbackSubject;
    @BindString(R.string.send_email) String sendEmail;

    private SettingsAdapter settingsAdapter;
    private NearbyNameManager nearbyNameManager;
    private ThemeManager themeManager = ThemeManager.get();
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.settings,
                container,
                false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nearbyNameManager = new NearbyNameManager(getActivity(), null);
        settingsOptions.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        settingsAdapter = new SettingsAdapter(getActivity(), this);
        settingsOptions.setAdapter(settingsAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(getActivity(), NearbySharingActivity.class);
                break;
            case 1:
                intent = new Intent(getActivity(), BackupAndRestoreActivity.class);
                break;
            case 2:
                View thirdCell = settingsOptions.getChildAt(2);
                Switch darkThemeToggle = thirdCell.findViewById(R.id.toggle);
                boolean darkThemeEnabled = darkThemeToggle.isChecked();
                darkThemeToggle.setChecked(!darkThemeEnabled);
                themeManager.setDarkModeEnabled(getContext(), !darkThemeEnabled);
                return;
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent csvIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                    String[] mimeTypes =
                            {"text/comma-separated-values",
                                    "text/csv",
                                    "application/csv",
                                    "application/excel",
                                    "application/vnd.ms-excel",
                                    "application/vnd.msexcel",
                                    "text/anytext"};

                    csvIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    csvIntent.setType("image/*");
                    csvIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(csvIntent, 1);
                }
                return;
            case 4:
                nearbyNameManager.showNameSetter();
                return;
            case 5:
                String uriText = "mailto:" + SUPPORT_EMAIL + "?subject=" + Uri.encode(feedbackSubject);
                Uri mailUri = Uri.parse(uriText);
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, mailUri);
                startActivity(Intent.createChooser(sendIntent, sendEmail));
                return;
            case 6:
                Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getString(R.string.share_app_message))
                        .getIntent();
                if (shareIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
                return;
            case 7:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(OTHER_APPS_URL));
                break;
            case 8:
                Uri uri =  Uri.parse("market://details?id=" + getContext().getPackageName());
                intent = new Intent(Intent.ACTION_VIEW, uri);
                if (!(getContext().getPackageManager().queryIntentActivities(intent, 0).size() > 0)) {
                    UIUtils.showLongToast(R.string.play_store_error, getContext());
                    return;
                }
                break;
            case 9:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(REPO_URL));
                break;
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        settingsAdapter.cleanUp();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.filter).setVisible(false);
    }
}
