package com.wilco375.settingseditor.object.serializable;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Serializable version of {@link Intent}
 */
public class SerializableIntent implements Serializable {
    public String mAction;
    public String mData;
    public String mType;
    public String mPackage;
    public String mComponentPkg;
    public String mComponentClass;
    public int mFlags;
    public HashSet<String> mCategories;
    public SerializableBundle mExtras;
    public int mSourceBoundsLeft;
    public int mSourceBoundsTop;
    public int mSourceBoundsRight;
    public int mSourceBoundsBottom;

    /**
     * Constructor for Jackson
     */
    public SerializableIntent(){ }

    /**
     * Create a serializable intent from a {@link Intent}
     * @param i intent to convert
     */
    public SerializableIntent(Intent i){
        if(i == null) return;
        mAction = i.getAction();
        if(i.getData() != null) mData = i.getData().toString();
        mType = i.getType();
        mPackage = i.getPackage();
        if(i.getComponent() != null){
            mComponentPkg = i.getComponent().getPackageName();
            mComponentClass = i.getComponent().getClassName();
        }
        mFlags = i.getFlags();
        if(i.getCategories() != null) {
            mCategories = new HashSet<>();
            mCategories.addAll(i.getCategories());
        }
        mExtras = new SerializableBundle(i.getExtras());
        if(i.getSourceBounds() != null) {
            Rect mSourceBounds = i.getSourceBounds();
            mSourceBoundsLeft = mSourceBounds.left;
            mSourceBoundsTop = mSourceBounds.top;
            mSourceBoundsRight = mSourceBounds.right;
            mSourceBoundsBottom = mSourceBounds.bottom;
        }
    }

    /**
     * Convert the serializable intent back to a {@link Intent}
     * @return the converted intent
     */
    public Intent toIntent(){
        Intent i = new Intent();
        i.setAction(mAction);
        if(mData != null) i.setData(Uri.parse(mData));
        i.setType(mType);
        i.setPackage(mPackage);
        if(mComponentPkg != null) i.setComponent(new ComponentName(mComponentPkg, mComponentClass));
        i.setFlags(mFlags);
        if(mCategories != null) {
            for (String category : mCategories) {
                i.addCategory(category);
            }
        }
        if(mExtras != null && mExtras.toBundle() != null) i.putExtras(mExtras.toBundle());
        i.setSourceBounds(new Rect(mSourceBoundsLeft, mSourceBoundsTop, mSourceBoundsRight, mSourceBoundsBottom));
        return i;
    }

    public static Intent getSettingsIntent(){
        SerializableIntent intent = new SerializableIntent();
        intent.mPackage = "com.android.settings";
        return intent.toIntent();
    }
}
