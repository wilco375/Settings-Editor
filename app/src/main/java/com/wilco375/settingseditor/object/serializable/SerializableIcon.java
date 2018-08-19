package com.wilco375.settingseditor.object.serializable;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Serializable version of {@link Icon}
 */
@TargetApi(Build.VERSION_CODES.N)
public class SerializableIcon implements Serializable {
    private static final int TYPE_BITMAP = 1;
    private static final int TYPE_RESOURCE = 2;
    private static final int TYPE_DATA = 3;
    private static final int TYPE_URI = 4;

    /**
     * An icon is a wrapper class for {@link Bitmap}, <code>Resource</code>, <code>Data Bytes</code> and {@link java.net.URI}.
     * This variable indicates the stored image type
     */
    public int type;

    /**
     * Package name if type is <code>Resource</code>, URI if type is {@link java.net.URI}, Base64 encoded bitmap if type is {@link Bitmap} or <code>Data Bytes</code>
     */
    public String str1;

    /**
     * Resource id if type is <code>Resource</code>
     */
    public int int1;

    /**
     * Constructor for Jackson
     */
    public SerializableIcon() {
    }

    /**
     * Create a serializable icon from a {@link Icon}
     *
     * @param icon    icon to convert
     * @param context application context
     */
    public SerializableIcon(Icon icon, Context context) {
        int type = (int) getObjectField(icon, "mType");
        this.type = type;
        switch (type) {
            case TYPE_BITMAP:
                saveBitmap((Bitmap) getObjectField(icon, "mObj1"));
                break;
            case TYPE_RESOURCE:
                str1 = (String) getObjectField(icon, "mString1");
                int1 = (int) getObjectField(icon, "mInt1");
                break;
            case TYPE_DATA:
                this.type = TYPE_BITMAP;
                Drawable drawable = icon.loadDrawable(context);
                saveBitmap(drawableToBitmap(drawable));
                break;
            case TYPE_URI:
                str1 = (String) getObjectField(icon, "mString1");
                break;
        }
    }

    /**
     * Convert the serializable icon back to a {@link Icon}
     *
     * @return the converted icon
     */
    public Icon toIcon() {
        switch (type) {
            case TYPE_BITMAP:
                return Icon.createWithBitmap(toBitmap());
            case TYPE_RESOURCE:
                return Icon.createWithResource(str1, int1);
            case TYPE_URI:
                return Icon.createWithContentUri(str1);
        }
        return null;
    }

    /**
     * Convenience method to save bitmap as a Base64 encoded string
     *
     * @param bits bitmap to save
     */
    private void saveBitmap(Bitmap bits) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bits.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        str1 = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Get the icon as bitmap
     *
     * @return bitmap of icon
     */
    @Nullable
    public Bitmap toBitmap() {
        return toBitmap(null);
    }

    /**
     * Get the icon as bitmap
     *
     * @param context application context
     * @return bitmap of icon
     */
    @Nullable
    public Bitmap toBitmap(@Nullable Context context) {
        if (type == TYPE_BITMAP) {
            byte[] byteArray = Base64.decode(str1, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else if (context != null) {
            if (type != TYPE_RESOURCE || int1 != 0x0) {
                return drawableToBitmap(toIcon().loadDrawable(context));
            }
        }
        return null;
    }

    /**
     * Convenience method to convert a drawable to a bitmap of at least 200px width
     *
     * @param drawable drawable to convert
     * @return converted drawable
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        if (width < 200) {
            height = 200 * height / width;
            width = 200;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
