package com.wilco375.settingseditor.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.general.PreferenceConstants;
import com.wilco375.settingseditor.general.PreferencesManager;
import com.wilco375.settingseditor.general.Utils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MainActivity.checkIntro(this);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title of toolbar and add back button
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle(R.string.settings);

        if (Utils.aboveOreo()) {
            findViewById(R.id.app_info).setVisibility(View.GONE);
        }

        configItems();
    }

    /**
     * Configurable items
     */
    private void configItems() {
        final CheckBox modifyColumnsCheckBox = findViewById(R.id.modifyColumnsCheckBox);
        final EditText modifyColumnsEditText = findViewById(R.id.modifyColumnsEditText);
        CheckBox iconsOnlyCheckBox = findViewById(R.id.iconsOnlyCheckBox);
        final CheckBox modifyIconSizeCheckBox = findViewById(R.id.modifyIconSizeCheckBox);
        final EditText modifyIconSizeEditText = findViewById(R.id.modifyIconSizeEditText);
        CheckBox removeIconBgCheckBox = findViewById(R.id.removeIconBgCheckBox);
        CheckBox installedAppsIconCheckBox = findViewById(R.id.installedAppsIconCheckBox);
        CheckBox showPackageCheckBox = findViewById(R.id.showPackageCheckBox);
        final CheckBox iconFilterCheckBox = findViewById(R.id.iconFilterCheckBox);
        final EditText iconFilterEditText = findViewById(R.id.iconFilterEditText);
        final CheckBox hideIconCheckBox = findViewById(R.id.hideIcon);
        final CheckBox backgroundColorCheckBox = findViewById(R.id.backgroundColorCheckBox);
        final EditText backgroundColorEditText = findViewById(R.id.backgroundColorEditText);
        final CheckBox textColorCheckBox = findViewById(R.id.textColorCheckBox);
        final EditText textColorEditText = findViewById(R.id.textColorEditText);
        final CheckBox hideStatusTextCheckBox = findViewById(R.id.hideStatusText);
        final CheckBox hideSuggestions = findViewById(R.id.hideSuggestions);
        final CheckBox debugCheckBox = findViewById(R.id.debug);

        final PreferencesManager prefs = PreferencesManager.getInstance();

        assert modifyColumnsCheckBox != null;
        modifyColumnsCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_COLUMN_COUNT, false));
        if (Utils.aboveNougat()) modifyColumnsCheckBox.setVisibility(View.GONE);
        assert modifyColumnsEditText != null;
        modifyColumnsEditText.setText(String.valueOf(prefs.getInteger(PreferenceConstants.KEY_INT_COLUMN_COUNT, 1)));
        if (Utils.aboveNougat()) modifyColumnsEditText.setVisibility(View.GONE);
        assert iconsOnlyCheckBox != null;
        iconsOnlyCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_ICONS_ONLY, false));
        assert modifyIconSizeCheckBox != null;
        modifyIconSizeCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_ICON_SIZE, false));
        assert modifyIconSizeEditText != null;
        modifyIconSizeEditText.setText(String.valueOf(prefs.getInteger(PreferenceConstants.KEY_INT_ICON_SIZE, 100)));
        assert removeIconBgCheckBox != null;
        removeIconBgCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_REMOVE_BACKGROUND, false));
        assert installedAppsIconCheckBox != null;
        installedAppsIconCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_INSTALLED_APP_ICON, false));
        assert showPackageCheckBox != null;
        showPackageCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_SHOW_PACKAGE, false));
        assert iconFilterCheckBox != null;
        iconFilterCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_FILTER_COLOR, false));
        assert iconFilterEditText != null;
        iconFilterEditText.setText(prefs.getString(PreferenceConstants.KEY_STRING_FILTER_COLOR, "#000000"));
        assert hideIconCheckBox != null;
        hideIconCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_HIDE_ICON, false));
        assert backgroundColorCheckBox != null;
        backgroundColorCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_BACKGROUND, false));
        assert backgroundColorEditText != null;
        backgroundColorEditText.setText(prefs.getString(PreferenceConstants.KEY_STRING_BACKGROUND, "#FFFFFF"));
        assert textColorCheckBox != null;
        textColorCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_TEXT_COLOR, false));
        assert textColorEditText != null;
        textColorEditText.setText(prefs.getString(PreferenceConstants.KEY_STRING_TEXT_COLOR, "#000000"));
        assert hideStatusTextCheckBox != null;
        hideStatusTextCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_HIDE_STATUS, false));
        assert hideSuggestions != null;
        hideSuggestions.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_HIDE_SUGGESTIONS, false));
        if (Utils.belowNougat()) {
            hideSuggestions.setVisibility(View.GONE);
        }
        assert debugCheckBox != null;
        debugCheckBox.setChecked(prefs.getBoolean(PreferenceConstants.KEY_BOOL_DEBUG, false));

        modifyColumnsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = modifyColumnsEditText.getText().toString();
            if (!text.equals(""))
                prefs.put(PreferenceConstants.KEY_INT_COLUMN_COUNT, Integer.valueOf(text));
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_COLUMN_COUNT, isChecked);
        });

        modifyColumnsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    prefs.put(PreferenceConstants.KEY_INT_COLUMN_COUNT, Integer.valueOf(s.toString()));
                prefs.putAndApply(PreferenceConstants.KEY_BOOL_COLUMN_COUNT, modifyColumnsCheckBox.isChecked());
            }
        });

        iconsOnlyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_ICONS_ONLY, isChecked));

        modifyIconSizeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = modifyIconSizeEditText.getText().toString();
            if (!text.equals(""))
                prefs.put(PreferenceConstants.KEY_INT_ICON_SIZE, Integer.valueOf(text));
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_ICON_SIZE, isChecked);
        });

        modifyIconSizeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    prefs.put(PreferenceConstants.KEY_INT_ICON_SIZE, Integer.valueOf(s.toString()));
                prefs.putAndApply(PreferenceConstants.KEY_BOOL_ICON_SIZE, modifyIconSizeCheckBox.isChecked());
            }
        });

        removeIconBgCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_REMOVE_BACKGROUND, isChecked));

        installedAppsIconCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_INSTALLED_APP_ICON, isChecked));

        showPackageCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_SHOW_PACKAGE, isChecked));

        iconsOnlyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_ICONS_ONLY, isChecked));

        iconFilterCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = iconFilterEditText.getText().toString();
            if (!text.equals(""))
                prefs.put(PreferenceConstants.KEY_STRING_FILTER_COLOR, text);
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_FILTER_COLOR, isChecked);
        });

        iconFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    prefs.put(PreferenceConstants.KEY_STRING_FILTER_COLOR, s.toString());
                prefs.putAndApply(PreferenceConstants.KEY_BOOL_FILTER_COLOR, iconFilterCheckBox.isChecked());
            }
        });

        hideIconCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName(SettingsActivity.this, "com.wilco375.settingseditor.MainActivityAlias");
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else {
                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName(SettingsActivity.this, "com.wilco375.settingseditor.MainActivityAlias");
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_HIDE_ICON, isChecked);
        });

        backgroundColorCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = backgroundColorEditText.getText().toString();
            if (!text.equals(""))
                prefs.put(PreferenceConstants.KEY_STRING_BACKGROUND, text);
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_BACKGROUND, isChecked);
        });

        backgroundColorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    prefs.put(PreferenceConstants.KEY_STRING_BACKGROUND, s.toString());
                prefs.putAndApply(PreferenceConstants.KEY_BOOL_BACKGROUND, backgroundColorCheckBox.isChecked());
            }
        });

        textColorCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = textColorEditText.getText().toString();
            if (!text.equals(""))
                prefs.put(PreferenceConstants.KEY_STRING_TEXT_COLOR, text);
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_TEXT_COLOR, isChecked);
        });

        textColorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    prefs.put(PreferenceConstants.KEY_STRING_TEXT_COLOR, s.toString());
                prefs.putAndApply(PreferenceConstants.KEY_BOOL_TEXT_COLOR, textColorCheckBox.isChecked());
            }
        });

        hideStatusTextCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_HIDE_STATUS, isChecked));

        hideSuggestions.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.putAndApply(PreferenceConstants.KEY_BOOL_HIDE_SUGGESTIONS, isChecked));

        debugCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.putAndApply(PreferenceConstants.KEY_BOOL_DEBUG, isChecked);

            // Kill settings
            ActivityManager am = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
            if (am != null) am.killBackgroundProcesses("com.android.settings");
        });
    }

    /**
     * Options menu inflater
     *
     * @param menu Menu to be inflated
     * @return success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * On option in menu selected
     *
     * @param item Item selected
     * @return success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (R.id.settings):
                // Kill and restart settings
                ActivityManager am = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
                if (am != null)
                    am.killBackgroundProcesses("com.android.settings");

                PackageManager pm = activity.getPackageManager();
                Intent i = pm.getLaunchIntentForPackage("com.android.settings");
                if (i != null) {
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    activity.startActivity(i);
                }
                return true;
            case (android.R.id.home):
                // call onBackPressed() on ActionBar back button pressed
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * On device or ActionBar back button pressed, close activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.hold, R.anim.right_slide_out);
    }
}
