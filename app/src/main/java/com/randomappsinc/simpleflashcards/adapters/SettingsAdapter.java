package com.randomappsinc.simpleflashcards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;
import com.randomappsinc.simpleflashcards.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsAdapter extends BaseAdapter {

    private String[] itemNames;
    private String[] itemIcons;
    private Context context;

    public SettingsAdapter(Context context) {
        this.context = context;
        this.itemNames = context.getResources().getStringArray(R.array.settings_options);
        this.itemIcons = context.getResources().getStringArray(R.array.settings_icons);
    }

    @Override
    public int getCount() {
        return itemNames.length;
    }

    @Override
    public String getItem(int position) {
        return itemNames[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class SettingsViewHolder {
        @BindView(R.id.icon) public IconTextView itemIcon;
        @BindView(R.id.option) public TextView itemName;

        SettingsViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public View getView(int position, View view, ViewGroup parent) {
        SettingsViewHolder holder;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.settings_item_cell, parent, false);
            holder = new SettingsViewHolder(view);
            view.setTag(holder);
        }
        else {
            holder = (SettingsViewHolder) view.getTag();
        }

        holder.itemName.setText(itemNames[position]);
        holder.itemIcon.setText(itemIcons[position]);

        return view;
    }
}
