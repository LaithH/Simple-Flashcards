package com.randomappsinc.simpleflashcards.dev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DevFeaturesTogglesAdapter
        extends RecyclerView.Adapter<DevFeaturesTogglesAdapter.FeatureToggleViewHolder> {

    public interface Listener {
        void onToggleClicked(String feature, boolean enabled);
    }

    protected Listener listener;
    protected List<String> featureNames;
    protected DevFeatureToggleManager featureToggleManager = DevFeatureToggleManager.get();

    public DevFeaturesTogglesAdapter(Listener listener) {
        this.listener = listener;
        this.featureNames = featureToggleManager.getAllFeatureToggles();
    }

    @NonNull
    @Override
    public FeatureToggleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.feature_toggle_cell,
                parent,
                false);
        return new FeatureToggleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureToggleViewHolder holder, int position) {
        holder.loadFeature(position);
    }

    @Override
    public int getItemCount() {
        return featureNames.size();
    }

    class FeatureToggleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.feature_name) TextView featureName;
        @BindView(R.id.feature_toggle) Switch featureToggle;

        FeatureToggleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void loadFeature(int position) {
            String featureNameText = featureNames.get(position);
            featureName.setText(featureNameText);
            Context context = featureName.getContext();
            UIUtils.setCheckedImmediately(
                    featureToggle, featureToggleManager.isFeatureEnabled(context, featureNameText));
        }

        @OnClick(R.id.feature_toggle_parent)
        public void onCellClicked() {
            boolean newStatus = !featureToggle.isChecked();
            featureToggle.setChecked(newStatus);
            listener.onToggleClicked(featureNames.get(getAdapterPosition()), newStatus);
        }

        @OnClick(R.id.feature_toggle)
        public void onToggleClicked() {
            listener.onToggleClicked(featureNames.get(getAdapterPosition()), featureToggle.isChecked());
        }
    }
}
