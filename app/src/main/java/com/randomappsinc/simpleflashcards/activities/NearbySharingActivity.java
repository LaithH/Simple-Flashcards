package com.randomappsinc.simpleflashcards.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.adapters.NearbyDevicesAdapter;
import com.randomappsinc.simpleflashcards.managers.NearbyConnectionsManager;
import com.randomappsinc.simpleflashcards.managers.NearbyNameManager;
import com.randomappsinc.simpleflashcards.models.NearbyDevice;
import com.randomappsinc.simpleflashcards.utils.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NearbySharingActivity extends StandardActivity {

    @BindView(R.id.location_permission_needed) View locationPrompt;
    @BindView(R.id.nearby_name_needed) View nearbyNameNeeded;
    @BindView(R.id.searching) View searching;
    @BindView(R.id.skeleton_devices_list) View skeletonDevicesList;
    @BindView(R.id.devices_list) RecyclerView devicesList;

    protected String nearbyName;
    protected NearbyNameManager nearbyNameManager;
    private NearbyConnectionsManager nearbyConnectionsManager;
    protected NearbyDevicesAdapter nearbyDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_sharing);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        nearbyNameManager = new NearbyNameManager(this, nameChangeListener);
        nearbyName = nearbyNameManager.getCurrentName();
        nearbyConnectionsManager = NearbyConnectionsManager.get();
        nearbyConnectionsManager.setListener(connectionsListener);

        nearbyDevicesAdapter = new NearbyDevicesAdapter(deviceChoiceListener);
        devicesList.setAdapter(nearbyDevicesAdapter);

        if (PermissionUtils.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            startSearching();
        } else {
            locationPrompt.setVisibility(View.VISIBLE);
            PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    1);
        }
    }

    @OnClick(R.id.grant_permission)
    public void askForPermission() {
        PermissionUtils.requestPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                1);
    }

    @OnClick(R.id.set_nearby_name_button)
    public void setNearbyName() {
        nearbyNameManager.showNameSetter();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSearching();
        }
    }

    protected void startSearching() {
        locationPrompt.setVisibility(View.GONE);
        if (TextUtils.isEmpty(nearbyName)) {
            nearbyNameNeeded.setVisibility(View.VISIBLE);
            nearbyNameManager.showNameSetter();
        } else {
            nearbyNameNeeded.setVisibility(View.GONE);
            searching.setVisibility(View.VISIBLE);
            nearbyConnectionsManager.startAdvertisingAndDiscovering(this);
        }
    }

    private final NearbyNameManager.Listener nameChangeListener = new NearbyNameManager.Listener() {
        @Override
        public void onNameChanged() {
            nearbyName = nearbyNameManager.getCurrentName();
            startSearching();
        }
    };

    private final NearbyConnectionsManager.Listener connectionsListener = new NearbyConnectionsManager.Listener() {
        @Override
        public void onNearbyDeviceFound(NearbyDevice device) {
            nearbyDevicesAdapter.addNearbyDevice(device);
            skeletonDevicesList.setVisibility(View.GONE);
        }

        @Override
        public void onNearbyDeviceLost(String endpointId) {
            nearbyDevicesAdapter.removeNearbyDevice(endpointId);
            if (nearbyDevicesAdapter.getItemCount() == 0) {
                skeletonDevicesList.setVisibility(View.VISIBLE);
            }
        }
    };

    private final NearbyDevicesAdapter.Listener deviceChoiceListener = new NearbyDevicesAdapter.Listener() {
        @Override
        public void onNearbyDeviceChosen(NearbyDevice device) {

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        nearbyConnectionsManager.stopAdvertisingAndDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nearbyConnectionsManager.shutdown();
    }
}
