package com.wilco375.settingseditor.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.adapter.TitleDescAdapter;
import com.wilco375.settingseditor.general.Utils;
import com.wilco375.settingseditor.licensing.License;
import com.wilco375.settingseditor.object.MainListItem;
import com.wilco375.settingseditor.xposed.XposedChecker;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static com.wilco375.settingseditor.BuildConfig.PRO;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkIntro(this);

        new License().checkLicense(this);

        // Set toolbar and add icon
        Toolbar toolbar = findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        // Set title of toolbar and add back button
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(R.string.app_name);

        final Activity activity = this;

        // List of options
        List<MainListItem> mainListViewItems = new ArrayList<>();

        mainListViewItems.add(new MainListItem(
                "settings",
                getResources().getString(R.string.settings),
                getResources().getString(R.string.settings_desc)));

        if(Utils.belowOreo()) {
            // Android Oreo doesn't have categories
            mainListViewItems.add(new MainListItem(
                    "categories",
                    getResources().getString(R.string.categories),
                    getResources().getString(R.string.categories_desc)));
        }

        // Items aren't supported on Oreo
        mainListViewItems.add(new MainListItem(
                "tiles",
                getResources().getString(R.string.tiles),
                getResources().getString(R.string.tiles_desc)));

        mainListViewItems.add(new MainListItem(
                "about",
                getResources().getString(R.string.about),
                ""));
        mainListViewItems.add(new MainListItem(
                "backup",
                getResources().getString(R.string.backup),
                getResources().getString(R.string.backup_desc)));

        // Start activity on item click
        ListView mainListView = findViewById(R.id.mainListView);
        TitleDescAdapter mainListAdapter = new TitleDescAdapter(this, mainListViewItems);
        mainListView.setAdapter(mainListAdapter);
        mainListView.setOnItemClickListener((parent, view, position, id) -> {
            switch(mainListAdapter.getItem(position).id){
                case "settings":
                    startActivity(new Intent(activity, SettingsActivity.class));
                    break;
                case "categories":
                    startActivity(new Intent(activity, CategoriesActivity.class));
                    break;
                case "tiles":
                    startActivity(new Intent(activity, TilesActivity.class));
                    break;
                case "about":
                    startActivity(new Intent(activity, AboutActivity.class));
                    break;
                case "backup":
                    startActivity(new Intent(activity, BackupActivity.class));
                    break;
            }
            overridePendingTransition(R.anim.right_slide_in, R.anim.hold);
        });

        // Set the FAB, and disable it if PRO
        FloatingActionButton proFab = findViewById(R.id.profab);
        if (PRO){
            proFab.setVisibility(View.GONE);
        }else{
            proFab.setOnClickListener(view -> startActivity(new Intent(ACTION_VIEW, Uri.parse(Utils.PRO_URL))));
        }

        // Show popup if module not activated
        if(!XposedChecker.active()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.xposed_not_active);
            builder.setMessage(R.string.xposed_not_active_desc);
            builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {

            });
            builder.show();
        }
    }

    public static void checkIntro(Context context){
        // On first boot, launch App Intro
        if (context.getSharedPreferences("sp", MODE_PRIVATE).getBoolean("firstAppLaunch",true) || (
                Utils.aboveMarshmallow() &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        )){
            Intent appIntro = new Intent(context, AppIntro.class);
            context.startActivity(appIntro);
        }
    }
}
