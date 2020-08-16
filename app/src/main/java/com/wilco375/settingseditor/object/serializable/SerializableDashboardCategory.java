package com.wilco375.settingseditor.object.serializable;

import android.content.Context;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.wilco375.settingseditor.xposed.DashboardHook;
import com.wilco375.settingseditor.xposed.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Serializable version of {@link com.android.settings.dashboard.DashboardCategory} or {@link com.android.settingslib.drawer.DashboardCategory} (Nougat)
 */
public class SerializableDashboardCategory implements Serializable {

    public static final long CAT_ID_UNDEFINED = -1;
    public long id = CAT_ID_UNDEFINED;
    public String title;
    public String key;
    public int externalIndex = -1;
    public List<SerializableDashboardTile> tiles = new ArrayList<>();
    public int priority = -1;

    public SerializableDashboardCategory() {
    }

    /**
     * Create a serializable dashboard category from a {@link {@link com.android.settings.dashboard.DashboardCategory}}
     *
     * @param dashboardCategory dashboard category to save
     * @param context           Settings context, used to get resources from Settings context
     * @param dashboardHook     Dashboard hook instance
     */
    public SerializableDashboardCategory(Object dashboardCategory, Context context, DashboardHook dashboardHook) {
        try {
            id = (long) getObjectField(dashboardCategory, "id");
        } catch (NoSuchFieldError e) {
        }
        title = dashboardHook.getTitle(dashboardCategory);
        try {
            key = (String) getObjectField(dashboardCategory, "key");
        } catch (NoSuchFieldError e) {
        }
        try {
            externalIndex = (int) getObjectField(dashboardCategory, "externalIndex");
        } catch (NoSuchFieldError e) {
        }
        try {
            priority = (int) getObjectField(dashboardCategory, "priority");
        } catch (NoSuchFieldError e) {
        }

        for (Object dashboardTile : dashboardHook.getTiles(dashboardCategory)) {
            try {
                tiles.add(new SerializableDashboardTile(dashboardTile, context, dashboardHook));
            } catch (NullPointerException e) {
                // Empty category name
            }
        }
    }

    /**
     * Convert the serializable dashboard category back to a {@link com.android.settings.dashboard.DashboardCategory}
     *
     * @param DashboardCategory {@link com.android.settings.dashboard.DashboardCategory} class of Settings from Xposed context
     * @param DashboardTile     {@link com.android.settings.dashboard.DashboardTile} class of Settings from Xposed context
     * @return the converted category
     */
    public Object toDashboardCategory(Class<?> DashboardCategory, Class<?> DashboardTile) {
        try {
            Object dashboardCategory = DashboardCategory.newInstance();
            if (id != 0x0 && id != CAT_ID_UNDEFINED) setObjectField(dashboardCategory, "id", id);
            if (title != null) setObjectField(dashboardCategory, "title", title);
            if (key != null) setObjectField(dashboardCategory, "key", key);
            if (externalIndex != -1)
                setObjectField(dashboardCategory, "externalIndex", externalIndex);
            if (priority != -1) setObjectField(dashboardCategory, "priority", priority);
            for (SerializableDashboardTile dashboardTile : tiles) {
                if (dashboardTile != null)
                    XposedHelpers.callMethod(dashboardCategory, "addTile", dashboardTile.toDashboardTile(DashboardTile));
            }
            return dashboardCategory;
        } catch (Throwable t) {
            Logger.logDbg("Error while converting DashboardCategory: " + t.toString());
            t.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "SerializableDashboardCategory{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", key='" + key + '\'' +
                ", externalIndex=" + externalIndex +
                ", tiles=" + tiles +
                ", priority=" + priority +
                '}';
    }

    private static BiMap<String, String> getKeyTitleMap() {
        BiMap<String, String> keyTitleMap = HashBiMap.create();
        keyTitleMap.put("com.android.settings.category.ia.homepage", "Homepage");
        keyTitleMap.put("com.android.settings.category.ia.wireless", "Network");
        keyTitleMap.put("com.android.settings.category.ia.device", "Device");
        keyTitleMap.put("com.android.settings.category.ia.apps", "Apps");
        keyTitleMap.put("com.android.settings.category.ia.apps.default", "Default Apps");
        keyTitleMap.put("com.android.settings.category.ia.battery", "Battery");
        keyTitleMap.put("com.android.settings.category.ia.display", "Display");
        keyTitleMap.put("com.android.settings.category.ia.sound", "Sound");
        keyTitleMap.put("com.android.settings.category.ia.storage", "Storage");
        keyTitleMap.put("com.android.settings.category.ia.security", "Security");
        keyTitleMap.put("com.android.settings.category.ia.lockscreen", "Lockscreen");
        keyTitleMap.put("com.android.settings.category.ia.accounts", "Accounts");
        keyTitleMap.put("com.android.settings.category.ia.account_detail", "Account Detail");
        keyTitleMap.put("com.android.settings.category.ia.system", "System");
        keyTitleMap.put("com.android.settings.category.ia.language", "Language");
        keyTitleMap.put("com.android.settings.category.ia.development", "Development");
        keyTitleMap.put("com.android.settings.category.ia.notifications", "Notifications");
        return keyTitleMap;
    }

    public String getTitle() {
        if (title != null && !title.equals("")) return title;
        else if (key != null && !key.equals("")) {
            String title = getKeyTitleMap().get(key);
            if (title != null) return title;
        }

        return "";
    }

    public String getKey(String title) {
        return getKeyTitleMap().inverse().get(title);
    }
}
