package com.wilco375.settingseditor.object;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * Object that stores the title, package and icon of an app
 */
public class AppItem implements Comparable<AppItem> {
    public AppItem() {
    }

    public AppItem(ResolveInfo ri, Context c) {
        this.drawable = ri.activityInfo.loadIcon(c.getPackageManager());
        this.title = ri.activityInfo.loadLabel(c.getPackageManager()).toString();
        this.pkg = ri.activityInfo.packageName;
    }

    public AppItem(String title, Drawable drawable, String pkg) {
        this.drawable = drawable;
        this.title = title;
        this.pkg = pkg;
    }

    /**
     * Title of the app
     */
    public String title;
    /**
     * Icon of the app
     */
    public Drawable drawable;
    /**
     * Package of the app
     */
    public String pkg;

    /**
     * Check if the two app items are the same by comparing the title
     *
     * @param appItem app item to compare
     * @return the value <code>0</code> if the two titles are equal;
     * a value less than <code>0</code> if this title is lexicographically less than the title argument;
     * and a value greater than <code>0</code> if this title is lexicographically greater than the title argument.
     */
    @Override
    public int compareTo(@NonNull AppItem appItem) {
        return this.title.compareTo(appItem.title);
    }
}




