package com.randomappsinc.simpleflashcards.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.persistence.PreferencesManager;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;
import com.randomappsinc.simpleflashcards.theme.ThemedIconTextView;
import com.randomappsinc.simpleflashcards.theme.ThemedTextView;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsAdapter
        extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>
        implements ThemeManager.Listener {

    public interface ItemSelectionListener {
        void onItemClick(int position);
    }

    @NonNull protected ItemSelectionListener itemSelectionListener;
    protected String[] options;
    protected String[] icons;
    protected PreferencesManager preferencesManager;
    protected ThemeManager themeManager;

    public SettingsAdapter(Context context, @NonNull ItemSelectionListener itemSelectionListener) {
        this.itemSelectionListener = itemSelectionListener;
        this.options = context.getResources().getStringArray(R.array.settings_options);
        this.icons = context.getResources().getStringArray(R.array.settings_icons);
        this.preferencesManager = new PreferencesManager(context);
        this.themeManager = ThemeManager.get();
        themeManager.registerListener(this);
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        notifyDataSetChanged();
    }

    public void cleanUp() {
        themeManager.unregisterListener(this);
    }

    @Override
    @NonNull
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.settings_item_cell,
                parent,
                false);
        return new SettingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        holder.loadSetting(position);
    }

    @Override
    public int getItemCount() {
        return options.length;
    }

    class SettingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon) ThemedIconTextView icon;
        @BindView(R.id.option) ThemedTextView option;
        @BindView(R.id.toggle) Switch toggle;

        SettingViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadSetting(int position) {
            option.setText(options[position]);
            icon.setText(icons[position]);
            adjustForDarkMode();

            if (position == 1) {
                UIUtils.setCheckedImmediately(toggle, preferencesManager.getDarkModeEnabled());
                toggle.setVisibility(View.VISIBLE);
            } else {
                toggle.setVisibility(View.GONE);
            }
        }

        void adjustForDarkMode() {
            icon.setProperColors();
            option.setProperTextColor();
        }

        @OnClick(R.id.toggle)
        void onToggle() {
            if (getAdapterPosition() == 1) {
                themeManager.setDarkModeEnabled(toggle.getContext(), toggle.isChecked());
            }
        }

        @OnClick(R.id.parent)
        void onSettingSelected() {
            itemSelectionListener.onItemClick(getAdapterPosition());
        }
    }
}
