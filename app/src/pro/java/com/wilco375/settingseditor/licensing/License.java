package com.wilco375.settingseditor.licensing;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.provider.Settings;
import android.util.Log;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.wilco375.settingseditor.BuildConfig;
import com.wilco375.settingseditor.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class License {
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAif/UDfThq2J9SAX01LYsEkZ3H9FHndyzaQRdVQy0CwBJvJ4ugnWMOgdx/fPZ8rgLS8001DQ98o2ZZ9M5Qf+SrtbIgp63gpVLkxif3dhYMN428R2MKxeGd/NCd5LDvWbtbItJO6VWnVGNmLvDZb7ftBRF6cICYRtGfH/fDQkxkKJgr7hVwPHAFGkPjrpC7qXezVvb1QV7BPysCSeUyB5wWaI+0F2O72ozayHKM1lWJoq2ZCJl1oYsncLjLScaywMIEwChWnl2oy44ywDWzl9U/r/NmHImcbtAkhHwdFHO9jLiyTKRZZVaIWgjWjrZEcXApDlOSZ5rkKBrOnqQ2/X0yQIDAQAB";
    private static final byte[] SALT = new byte[] {30, 38, 55, 24, 43, 94, 70, 52, 49, 15};

    boolean licensed;
    boolean checkingLicense;
    boolean didCheck;
    Activity activity;

    @Nullable ProgressDialog licenseDialog;

    public void checkLicense(Activity activity) {
        if(!licensed && !checkingLicense && !BuildConfig.DEBUG) {
            didCheck = false;
            checkingLicense = true;
            this.activity = activity;

            showLicenseCheckingDialog();

            String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.i("Device Id", deviceId);

            LicenseCheckerCallback licenseCheckerCallback = new MyLicenseCheckerCallback();
            LicenseChecker checker = new LicenseChecker(activity, new ServerManagedPolicy(activity, new AESObfuscator(SALT, activity.getPackageName(), deviceId)), BASE64_PUBLIC_KEY);

            checker.checkAccess(licenseCheckerCallback);
        }
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            Log.i("License", "Accepted!");

            licensed = true;
            checkingLicense = false;
            didCheck = true;

            if(licenseDialog != null){
                licenseDialog.cancel();
            }
        }

        @Override
        public void dontAllow(int reason) {
            Log.i("License", "Denied!");
            Log.i("License", "Reason for denial: " + reason);

            licensed = false;
            checkingLicense = false;
            didCheck = true;

            showLicenseFailDialog(reason);
        }

        @Override
        public void applicationError(int reason) {
            Log.i("License", "Application error!");

            licensed = true;
            checkingLicense = false;
            didCheck = false;

            showLicenseFailDialog(-1);
        }
    }

    private void showLicenseFailDialog(final int reason){
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(false);
            builder.setTitle(R.string.license_fail_title);
            builder.setMessage(activity.getResources().getString(R.string.license_fail_desc, reason));
            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> System.exit(0));
            builder.setNeutralButton(R.string.retry, (dialog, which) -> {
                checkLicense(activity);
                dialog.cancel();
            });
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);

            if(licenseDialog != null){
                licenseDialog.cancel();
            }

            dialog.show();
        });
    }

    private void showLicenseCheckingDialog(){
        activity.runOnUiThread(() -> {
            licenseDialog = new ProgressDialog(activity);
            licenseDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            licenseDialog.setMessage(activity.getResources().getString(R.string.checking_license));
            licenseDialog.setIndeterminate(true);
            licenseDialog.setCanceledOnTouchOutside(false);
            licenseDialog.setCancelable(false);
            licenseDialog.setProgressNumberFormat(null);
            licenseDialog.setProgressPercentFormat(null);
            licenseDialog.show();
        });
    }
}
