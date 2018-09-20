package com.wilco375.settingseditor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wilco375.settingseditor.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title of toolbar and add back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.about);

        // Add contributors to TextView
        TextView contributors = findViewById(R.id.contributors);
        assert contributors != null;
        contributors.setText(
                contributors.getText() +
                        "\nDaniel THIRION\nJulia Yanovskaya\nBreno Rafael Corrêa\nTorsten Schneider\n~El Julio~\nMarco\nWoytazzer\nFatih Fırıncı\nLuis Gustavo Sanabria\nBhianzMuhh\nJose Ah Fer\n\n" +
                        "Google - Material Icons\nCarl A. Bauer - Drag Sort ListView\nPaolo Rotolo - AppIntro\nFasterXML - Jackson"
        );
    }

    /**
     * Open url on Xposed button clicked
     *
     * @param view clicked view
     */
    public void onXposedClick(View view) {
        openUrl("http://repo.xposed.info/module/com.wilco375.settingseditor");
    }

    /**
     * Open url on XDA button clicked
     *
     * @param view clicked view
     */
    public void onXdaClick(View view) {
        openUrl("http://forum.xda-developers.com/xposed/modules/xposed-settings-editor-easily-add-edit-t3365756");
    }

    /**
     * Open url on Website button clicked
     *
     * @param view clicked view
     */
    public void onWebsiteClick(View view) {
        openUrl("https://wilco375.com");
    }

    /**
     * Open url on bug form button clicked
     *
     * @param view clicked view
     */
    public void onBugClick(View view) {
        openUrl("https://wilco375.com/settingseditorsupport/");
    }

    /**
     * On option in ActionBar selected
     *
     * @param item Item selected
     * @return success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
