package com.randomappsinc.simpleflashcards.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.randomappsinc.simpleflashcards.R;
import com.randomappsinc.simpleflashcards.theme.ThemeManager;
import com.randomappsinc.simpleflashcards.utils.UIUtils;

public class StandardActivity extends AppCompatActivity implements ThemeManager.Listener {

    protected ThemeManager themeManager = ThemeManager.get();
    private int blue;
    private int darkBlue;
    private int actionBarBlack;
    private int statusBarBlack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blue = ContextCompat.getColor(this, R.color.app_blue);
        darkBlue = ContextCompat.getColor(this, R.color.dark_blue);
        actionBarBlack = ContextCompat.getColor(this, R.color.dark_mode_black);
        statusBarBlack = ContextCompat.getColor(this, R.color.dark_mode_status_bar_black);
        setActionBarColors();
        themeManager.registerListener(this);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        UIUtils.closeKeyboard(this);
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void finish() {
        UIUtils.closeKeyboard(this);
        super.finish();
        overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
    }

    protected void setActionBarColors() {
        boolean darkModeEnabled = themeManager.getDarkModeEnabled(this);
        if (getSupportActionBar() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(darkModeEnabled ? actionBarBlack : blue);
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(darkModeEnabled ? statusBarBlack : darkBlue);
        }
    }

    @Override
    public void onThemeChanged(boolean darkModeEnabled) {
        setActionBarColors();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
