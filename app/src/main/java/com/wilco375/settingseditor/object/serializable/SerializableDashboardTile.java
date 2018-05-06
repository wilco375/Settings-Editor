package com.wilco375.settingseditor.object.serializable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;

import com.wilco375.settingseditor.general.Utils;
import com.wilco375.settingseditor.xposed.DashboardHook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Serializable version of {@link com.android.settings.dashboard.DashboardTile} or {@link com.android.settingslib.drawer.Tile} (Nougat)
 */
public class SerializableDashboardTile implements Serializable {
    public static final long TILE_ID_UNDEFINED = -1;
    public long id = TILE_ID_UNDEFINED;
    public String title;
    public int summaryRes;
    public String summary;
    public int iconRes;
    public String iconPkg;
    public SerializableIcon icon;
    public String switchControl;
    public String fragment;
    public SerializableBundle fragmentArguments;
    public SerializableIntent intent;
    public SerializableBundle extras;
    public String category;
    public SerializableBundle metaData;
    public String key;
    public SerializableUserHandle[] userHandles;

    public String iconPath;
    public boolean modified = false;

    public SerializableDashboardTile(){ }

    /**
     * Create a serializable dashboard tile from a {@link {@link com.android.settings.dashboard.DashboardTile}}
     * @param dashboardTile dashboard tile to save
     * @param context Settings context, used to get resources from Settings context
     */
    public SerializableDashboardTile(Object dashboardTile, Context context, DashboardHook dashboardHook) throws NullPointerException {
        try {
            id = (long) getObjectField(dashboardTile, "id");
        } catch (NoSuchFieldError e) { }
        title = dashboardHook.getTitle(dashboardTile);
        if(!Utils.notEmpty(title)) throw new NullPointerException();
        try {
            summaryRes = (int) getObjectField(dashboardTile, "summaryRes");
        } catch (NoSuchFieldError e) { }
        try {
            summary = getObjectField(dashboardTile, "summary").toString();
        } catch (Throwable t) { }
        try {
            iconRes = (int) getObjectField(dashboardTile, "iconRes");
        } catch (Throwable t) { }
        try {
            if(Utils.aboveMarshmallow()) icon = new SerializableIcon((Icon) getObjectField(dashboardTile, "icon"), context);
        } catch (Throwable t) { }
        try {
            iconPkg = (String) getObjectField(dashboardTile, "iconPkg");
        } catch (NoSuchFieldError e) { }
        try {
            switchControl = (String) getObjectField(dashboardTile, "switchControl");
        } catch (NoSuchFieldError e) { }
        try {
            fragment = (String) getObjectField(dashboardTile, "fragment");
            fragmentArguments = new SerializableBundle((Bundle) getObjectField(dashboardTile, "fragmentArguments"));
        } catch (NoSuchFieldError e) { }
        intent = new SerializableIntent((Intent) getObjectField(dashboardTile, "intent"));
        try {
            extras = new SerializableBundle((Bundle) getObjectField(dashboardTile, "extras"));
        } catch (NoSuchFieldError e) { }
        try {
            category = (String) getObjectField(dashboardTile, "category");
        } catch (NoSuchFieldError e) { }
        try {
            metaData = new SerializableBundle((Bundle) getObjectField(dashboardTile, "metaData"));
        } catch (NoSuchFieldError e) { }
        try {
            key = (String) getObjectField(dashboardTile, "key");
        } catch (NoSuchFieldError e) { }
        if(Utils.aboveOreo()) {
            try {
                List<UserHandle> userHandles = (List<UserHandle>) new SerializableUserHandle((UserHandle) getObjectField(dashboardTile, "userHandles"));
                this.userHandles = new SerializableUserHandle[userHandles.size()];
                for(int i = 0; i < userHandles.size(); i++){
                    this.userHandles[i] = new SerializableUserHandle(userHandles.get(i));
                }
            } catch (NoSuchFieldError e) { }
        }
    }

    /**
     * Convert the serializable dashboard tile back to a {@link com.android.settings.dashboard.DashboardCategory}
     * @param DashboardTile {@link com.android.settings.dashboard.DashboardTile} class of Settings from Xposed context
     * @return the converted tile
     */
    public Object toDashboardTile(Class<?> DashboardTile){
        try {
            Object dashboardTile = DashboardTile.newInstance();
            if(id != 0x0 && id != TILE_ID_UNDEFINED) setObjectField(dashboardTile, "id", id);
            if(title != null) setObjectField(dashboardTile, "title", title);
            if(summaryRes != 0x0) setObjectField(dashboardTile, "summaryRes", summaryRes);
            if(summary != null) setObjectField(dashboardTile, "summary", summary);
            if(iconRes != 0x0) setObjectField(dashboardTile, "iconRes", iconRes);
            if(iconPkg != null) setObjectField(dashboardTile, "iconPkg", iconPkg);
            if(switchControl != null) setObjectField(dashboardTile, "switchControl", switchControl);
            if(fragment != null) setObjectField(dashboardTile, "fragment", fragment);
            if(fragmentArguments != null) setObjectField(dashboardTile, "fragmentArguments", fragmentArguments.toBundle());
            if(intent != null) setObjectField(dashboardTile, "intent", intent.toIntent());
            if(extras != null) setObjectField(dashboardTile, "extras", extras.toBundle());
            if(category != null) setObjectField(dashboardTile, "category", category);
            if(metaData != null) setObjectField(dashboardTile, "metaData", metaData.toBundle());
            if(key != null) setObjectField(dashboardTile, "key", key);
            if(Utils.aboveNougat()) {
                if(icon != null) setObjectField(dashboardTile, "icon", icon.toIcon());
                else {
                    try {
                        setObjectField(dashboardTile, "icon", Icon.createWithBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)));
                    } catch (NoSuchFieldError e) { }
                }
            }
            try{
                setObjectField(dashboardTile, "action", "");
            }catch (NoSuchFieldError e) {}

            if(Utils.aboveOreo()) {
                try {
                    List<UserHandle> handles;
                    if(userHandles != null){
                        handles = new ArrayList<>();
                        for(SerializableUserHandle handle : userHandles){
                            handles.add(handle.toUserHandle());
                        }
                    }else{
                        handles = null;
                    }
                    setObjectField(dashboardTile, "userHandle", handles);
                } catch (NoSuchFieldError e) { }
            }
            return dashboardTile;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "SerializableDashboardTile{" +
                "title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }
}
