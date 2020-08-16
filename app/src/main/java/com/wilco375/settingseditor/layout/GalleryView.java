package com.wilco375.settingseditor.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * View that displays images from assets in a gallery grid
 */
public class GalleryView extends GridView {
    public GalleryView(Context context) {
        super(context);
        setDefaults();
    }

    public GalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaults();
    }

    public GalleryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaults();
    }

    @TargetApi(21)
    public GalleryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setDefaults();
    }

    /**
     * Set default layout of view. Called in constructors
     */
    private void setDefaults() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setPadding(15, 15, 15, 15);
        setNumColumns(isTablet() ? 8 : 4);
        setColumnWidth(AUTO_FIT);
    }

    /**
     * Get images from assets based on filenames
     *
     * @param fileNames list of filenames in assets
     */
    public void setImagesFromAssets(List<String> fileNames) {
        setAdapter(new ImageAdapter(getContext(), fileNames));
    }

    /**
     * Set amount of columns depending on the device (<code>tablet = 8, phone = 4</code>)
     */
    @Override
    public void updateViewLayout(@Nullable View view, @Nullable ViewGroup.LayoutParams params) {
        if (view != null || params != null) super.updateViewLayout(view, params);
        setNumColumns(isTablet() ? 8 : 4);
    }

    /**
     * Check if the current device is a tablet
     *
     * @return true if display diagonal is at least 7 inches
     */
    private boolean isTablet() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        return diagonalInches >= 7;
    }

    /**
     * Adapter to show images from assets
     */
    private class ImageAdapter extends BaseAdapter {
        private Context context;
        protected List<String> fileNames;

        /**
         * @param context   application context
         * @param fileNames list of filenames of images in assets
         */
        public ImageAdapter(Context context, List<String> fileNames) {
            this.context = context;
            this.fileNames = fileNames;
        }

        /**
         * Get the amount of items the adapter is holding
         *
         * @return item count
         */
        public int getCount() {
            return fileNames.size();
        }

        /**
         * Get an item at a specific position
         *
         * @return item at position
         */
        public Object getItem(int position) {
            return fileNames.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        /**
         * Get the view at a specific position
         *
         * @param position    position of the view
         * @param convertView existing view at the position
         * @param parent      parent view
         * @return view at position
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // Only create the ImageView if it doesn't exist yet
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_START);
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(15, 15, 15, 15);
            } else {
                imageView = (ImageView) convertView;
            }
            try {
                imageView.setImageDrawable(Drawable.createFromStream(getContext().getAssets().open(fileNames.get(position)), null));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return imageView;
        }
    }
}
