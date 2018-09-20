package com.wilco375.settingseditor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.wilco375.settingseditor.BuildConfig;
import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.general.DashboardManager;
import com.wilco375.settingseditor.general.IOManager;
import com.wilco375.settingseditor.general.PreferenceConstants;
import com.wilco375.settingseditor.general.PreferencesManager;
import com.wilco375.settingseditor.general.Utils;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardCategory;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public List<SerializableDashboardCategory> categories = new ArrayList<>();
    public Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        getTiles();
    }

    /**
     * Get tiles from storage
     */
    public void getTiles() {
        categories = DashboardManager.getTiles();
        if (categories == null || categories.size() == 0) {
            categories = new ArrayList<>();
            showLaunchSettingsDialog();
        }
    }

    /**
     * Save tile changes to storage
     */
    public void saveTiles() {
        DashboardManager.saveTiles(categories);
    }

    /**
     * Check amount of modifications
     *
     * @return true if too many modifications
     */
    public boolean checkModifications() {
        int modifications = PreferencesManager.getInstance().getInteger(PreferenceConstants.KEY_MODIFICATIONS, 0);

        if (modifications >= 5 && !BuildConfig.PRO) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.trial_max_reached_title)
                    .setMessage(R.string.trial_max_reached_text)
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.PRO_URL)));
                        dialogInterface.cancel();
                    })
                    .create().show();

            return true;
        }
        return false;
    }

    /**
     * Kill com.android.settings and launch it
     */
    private void showLaunchSettingsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.launch_settings_title);
        builder.setMessage(R.string.launch_settings_text);
        builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
            IOManager.writeObject(new Object(), IOManager.READ_SETTINGS_TILES);
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage("com.android.settings");
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(intent);
                System.exit(0);
            }
        });
        builder.create().show();
    }
}
