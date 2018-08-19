package com.wilco375.settingseditor.general;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.wilco375.settingseditor.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static utility methods
 */
public class Utils {
    /**
     * Google Play Store url of the Pro version of the app
     */
    public static final String PRO_URL = "https://play.google.com/store/apps/details?id=com.wilco375.settingseditorpro";

    /**
     * Show the file chooser
     *
     * @param a Current activity
     */
    public static void showFileChooser(Activity a) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            a.startActivityForResult(
                    Intent.createChooser(intent, a.getResources().getString(R.string.select_icon)), 0);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(a, a.getResources().getString(R.string.install_fm_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get bitmap from Uri
     *
     * @param uri Uri to get bitmap from
     * @param c   Context of current activity
     * @return Bitmap bitmap from Uri
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context c) throws IOException, NullPointerException {
        ParcelFileDescriptor parcelFileDescriptor =
                c.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();

        if (image == null) throw new NullPointerException();
        return image;
    }

    /**
     * Check if a String is not null and not empty
     *
     * @param str String to check
     * @return String not null or empty
     */
    public static boolean notEmpty(String str) {
        return str != null && !str.equals("");
    }

    /**
     * Remove duplicates from List<String>
     *
     * @param originalList List<String> to remove duplicates from
     * @return New list with duplicates removed
     */
    public static List<String> removeDuplicates(List<String> originalList) {
        List<String> list = new ArrayList<>();
        list.addAll(originalList);
        ArrayList<String> ar = new ArrayList<>();
        while (list.size() > 0) {
            ar.add(list.get(0));
            list.removeAll(Collections.singleton(list.get(0)));
        }
        list.addAll(ar);
        return list;
    }

    /**
     * Convert display pixels to pixels
     *
     * @param dp      display pixels to convert to pixels
     * @param context context
     * @return converted pixels
     */
    public static int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * @return true if SDK version is below Oreo (SDK < 26)
     */
    public static boolean belowOreo() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    /**
     * @return true if SDK version is below Nougat (SDK < 24)
     */
    public static boolean belowNougat() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N;
    }

    /**
     * @return true if SDK version is at least Oreo (SDK >= 26)
     */
    public static boolean aboveOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * @return true if SDK version is at least Nougat (SDK >= 24)
     */
    public static boolean aboveNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    /**
     * @return true if SDK version is at least Marshmallow (SDK >= 23)
     */
    public static boolean aboveMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * @return true if SDK version is at least Marshmallow (SDK >= 23)
     */
    public static boolean aboveLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * @return true if SDK version is at least Jelly Bean MR1 (SDK >= 17)
     */
    public static boolean aboveJellybean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }
}
