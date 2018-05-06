package com.wilco375.settingseditor.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.github.paolorotolo.appintro.AppIntroFragment;
import com.wilco375.settingseditor.BuildConfig;
import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.general.Utils;

/**
 * Added by _cab13_
 *
 * Intro for asking permissions, and introducing the app
 */
public class AppIntro extends com.github.paolorotolo.appintro.AppIntro {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setProgressButtonEnabled(false);
        setGoBackLock(true);
        setSwipeLock(false);
        showSkipButton(false);

        //noinspection ConstantConditions
        addSlide(
                AppIntroFragment.newInstance(
                        getString(R.string.app_name),
                        getString(R.string.xposed_desc),
                        BuildConfig.PRO ? R.drawable.icon_pro : R.drawable.icon,
                        Color.parseColor("#F44336")
                )
        );

        if (BuildConfig.PRO) {
            addSlide(
                    AppIntroFragment.newInstance(
                            getString(R.string.pro_intro_title),
                            getString(R.string.thanks_pro_intro),
                            R.drawable.pro,
                            Color.parseColor("#F44336")
                    )
            );
        } else {
            addSlide(
                    AppIntroFragment.newInstance(
                            getString(R.string.free_intro_title),
                            getString(R.string.limited_free_intro),
                            R.drawable.cart,
                            Color.parseColor("#F44336")
                    )
            );
        }

        addSlide(
                AppIntroFragment.newInstance(
                        getString(R.string.edit_settings),
                        getString(R.string.edit_settings_desc),
                        R.drawable.intro,
                        Color.parseColor("#F44336")
                )
        );

        if (Utils.aboveMarshmallow()) {
            addSlide(
                    AppIntroFragment.newInstance(
                            getString(R.string.permissions_needed),
                            getString(R.string.permissions_needed_desc),
                            R.drawable.permissions,
                            Color.parseColor("#F44336")
                    )
            );
            askForPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 4);
        }

        if (BuildConfig.PRO && isInstalled("com.wilco375.settingseditor")) {
            addSlide(
                    AppIntroFragment.newInstance(
                            getString(R.string.remove_free),
                            getString(R.string.remove_free_desc),
                            R.drawable.trash,
                            Color.parseColor("#F44336")
                    )
            );
        }

        addSlide(
                AppIntroFragment.newInstance(
                        getString(R.string.app_ready),
                        getString(R.string.app_ready_desc),
                        R.drawable.all_done,
                        Color.parseColor("#F44336")
                )
        );
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if(oldFragment != null && oldFragment.getArguments() != null) {
            String title = oldFragment.getArguments().getString("title");
            if (title != null && title.equals(getString(R.string.remove_free))) {
                // Remove Settings Editor
                //Intent i = new Intent();
                //i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //i.setData(Uri.parse("package:com.wilco375.settingseditor"));
                //startActivity(i);

                Intent intent = new Intent(
                        Intent.ACTION_DELETE,
                        Uri.fromParts("package", "com.wilco375.settingseditor", "com.wilco375.settingseditor.MainActivity")
                );
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do Nothing
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        getSharedPreferences("sp", MODE_PRIVATE).edit().putBoolean("firstAppLaunch",false).apply();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED){
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder
                    .setTitle(R.string.permissions_needed)
                    .setMessage(R.string.permissions_denied)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        if (Utils.aboveMarshmallow()) {
                            requestPermissions(new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            }, 0);
                        }
                    })
                    .setNegativeButton(R.string.exit, (dialogInterface, i) -> System.exit(0));
            AlertDialog permAlert = alertBuilder.create();
            permAlert.setCancelable(false);
            permAlert.setCanceledOnTouchOutside(false);
            permAlert.show();
        }
    }

    private boolean isInstalled(String pkg) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            System.out.println(info.applicationInfo.packageName);
            System.out.println(info.applicationInfo.processName);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
