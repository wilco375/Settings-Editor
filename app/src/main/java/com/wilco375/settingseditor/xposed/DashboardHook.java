package com.wilco375.settingseditor.xposed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.wilco375.settingseditor.BuildConfig;
import com.wilco375.settingseditor.general.DashboardManager;
import com.wilco375.settingseditor.general.IOManager;
import com.wilco375.settingseditor.general.Utils;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardCategory;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardTile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Xposed hooks related to modifying the categories and tiles of the dashboard (home screen of Settings)
 */
public class DashboardHook {
    private Class<?> DashboardTile;
    private Class<?> DashboardCategory;
    private Class<?> DashboardSummary;
    private Class<?> DashboardAdapter;

    /**
     * List of dashboard categories of Settings Editor
     */
    private List<SerializableDashboardCategory> serializableDashboardCategoryList;

    /**
     * Context of the Settings app
     */
    private Context settingsActivityContext;

    /**
     * True if Settings should start Settings Editor
     */
    private boolean restart;

    /**
     * Initialize dashboard hook
     */
    DashboardHook() {
    }

    /**
     * Handle loading of Settings
     *
     * @param lpparam load package parameter
     */
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        Logger.logDbg("Hooking methods to change the Dashboard items");

        // Find all necessary classes
        Class<?> SettingsActivity;
        try {
            SettingsActivity = findClass("com.android.settings.SettingsActivity", lpparam.classLoader);
        } catch (Throwable e) {
            // HTC
            SettingsActivity = findClass("com.android.settings.hi", lpparam.classLoader);
        }
        if (Utils.belowNougat()) {
            // Marshmallow and below
            DashboardSummary = findClass("com.android.settings.dashboard.DashboardSummary", lpparam.classLoader);
            DashboardTile = findClass("com.android.settings.dashboard.DashboardTile", lpparam.classLoader);
            DashboardCategory = findClass("com.android.settings.dashboard.DashboardCategory", lpparam.classLoader);
        } else {
            // Nougat
            DashboardTile = findClass("com.android.settingslib.drawer.Tile", lpparam.classLoader);
            DashboardCategory = findClass("com.android.settingslib.drawer.DashboardCategory", lpparam.classLoader);
            DashboardAdapter = findClass("com.android.settings.dashboard.DashboardAdapter", lpparam.classLoader);
        }

        // Hook methods
        findAndHookMethod(SettingsActivity, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Logger.logDbg("SettingsActivity onCreate");
                if (settingsActivityContext == null)
                    settingsActivityContext = (Activity) param.thisObject;

                if (restart) {
                    startSettingsEditor();
                }
            }
        });

        if (Utils.belowNougat()) {
            // Marshmallow and below
            try {
                // ZTE, has to be hooked first because it also has a getDashboardCategories(boolean b) method which does nothing
                findAndHookMethod(SettingsActivity, "getDashboardCategories", int.class, boolean.class, getDashboardCategoriesHook);
            } catch (Throwable t) {
                try {
                    // Android 6.0
                    findAndHookMethod(SettingsActivity, "getDashboardCategories", boolean.class, getDashboardCategoriesHook);
                } catch (Throwable t0) {
                    try {
                        // Touchwiz
                        findAndHookMethod(SettingsActivity, "getDashboardCategories", boolean.class, boolean.class, getDashboardCategoriesHook);
                    } catch (Throwable t1) {
                        try {
                            // Touchwiz
                            findAndHookMethod(SettingsActivity, "getDashboardCategories", boolean.class, String.class, getDashboardCategoriesHook);
                        } catch (Throwable t2) {
                            try {
                                // Android 6.0
                                findAndHookMethod(SettingsActivity, "loadCategoriesFromResource", int.class, List.class, Context.class, loadCategoriesFromResourceHook);
                            } catch (Throwable t3) {
                                try {
                                    // Android 5.0
                                    findAndHookMethod(SettingsActivity, "loadCategoriesFromResource", int.class, List.class, loadCategoriesFromResourceHook);
                                } catch (Throwable t4) {
                                    try {
                                        // MIUI, obfuscated, so won't work for every MIUI Settings app
                                        findAndHookMethod("com.android.settings.gF", lpparam.classLoader, "l", List.class, new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                List<Object> categories = (List<Object>) param.args[0];
                                                param.args[0] = modifyDashboard(categories);
                                            }
                                        });
                                    } catch (Throwable t5) {
                                        XposedBridge.log("[Settings Editor] Error hooking loadCategoriesFromResource");
                                        XposedBridge.log(t3);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try {
                // Stock Android
                findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, updateTileViewHook);
            } catch (Throwable t) {
                try {
                    // CyanogenMod
                    findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, Switch.class, updateTileViewHook);
                } catch (Throwable t1) {
                    try {
                        // Touchwiz
                        findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, int.class, updateTileViewHook);
                        findAndHookMethod(SettingsActivity, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, int.class, updateTileViewHook);
                    } catch (Throwable t2) {
                        try {
                            // Touchwiz
                            findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, int.class, updateTileViewHook);
                            findAndHookMethod(SettingsActivity, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, int.class, updateTileViewHook);
                        } catch (Throwable t3) {
                            try {
                                // Sony
                                findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, updateTileViewHook);
                            } catch (Throwable t4) {
                                try {
                                    // Other method name
                                    Method[] updateTileView = findMethodsByExactParameters(DashboardSummary, void.class, Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class);
                                    Logger.logDbg("Found " + updateTileView.length + " matches using findMethodsByExactParameters to find updateTileView");
                                    if (updateTileView.length == 1) {
                                        Logger.logDbg("Hooking method with the name " + updateTileView[0].getName());
                                        findAndHookMethod(DashboardSummary, updateTileView[0].getName(), Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, updateTileViewHook);
                                    }
                                } catch (Throwable t5) {
                                    Logger.logDbg("Error hooking updateTileView");
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Nougat / Oreo

            try {
                // Oreo
                findAndHookMethod("com.android.settingslib.drawer.TileUtils", lpparam.classLoader, "getCategories", Context.class, Map.class, boolean.class, String.class, String.class, getCategories);
            } catch (Throwable t) {
                try {
                    // Nougat
                    findAndHookMethod("com.android.settingslib.drawer.TileUtils", lpparam.classLoader, "getCategories", Context.class, HashMap.class, boolean.class, getCategories);
                } catch (Throwable t1) {
                    try {
                        // Nougat
                        findAndHookMethod("com.android.settingslib.drawer.TileUtils", lpparam.classLoader, "getCategories", Context.class, HashMap.class, getCategories);
                    } catch (Throwable t2) {
                        Logger.logDbg("getCategories not found");
                    }
                }
            }

            try {
                findAndHookMethod(DashboardAdapter, "onBindTile", "com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", "com.android.settingslib.drawer.Tile", onBindTile);
            } catch (Throwable t) {
                Logger.logDbg("onBindTile not found");
            }

            if (Utils.aboveOreo()) {
                try {
                    findAndHookMethod(findClass("com.android.settingslib.drawer.Tile", lpparam.classLoader), "writeToParcel", Parcel.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (getObjectField(param.thisObject, "userHandle") == null) {
                                setObjectField(param.thisObject, "userHandle", new ArrayList<>());
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            List userHandle = (List) getObjectField(param.thisObject, "userHandle");
                            if (userHandle != null && userHandle.size() == 0) {
                                setObjectField(param.thisObject, "userHandle", null);
                            }
                        }
                    });
                } catch (Throwable t) {
                    Logger.logDbg("Failed to block sort and filter: " + t.getMessage());
                }

                try {
                    findAndHookMethod(findClass("com.android.settings.drawer.TileUtils", lpparam.classLoader), "getTilesForIntent", Context.class, UserHandle.class, Intent.class, Map.class, String.class, List.class, boolean.class, boolean.class, boolean.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Logger.logDbg("Setting shouldUpdateTiles to true");
                            param.args[9] = true;
                        }
                    });
                } catch (Throwable t) {
                    Logger.logDbg("Failed to set shouldUpdateTiles to true");
                }
            }
        }
    }

    /**
     * Dump categories array to Xposed Log
     *
     * @param categoriesObj categories array from Settings
     */
    private void logCategories(Object categoriesObj) {
        Logger.logDbg("########## Categories Dump ##########");
        List<Object> categories = (List<Object>) categoriesObj;
        for (Object category : categories) {
            List<Object> tiles = (List<Object>) getObjectField(category, "tiles");
            for (Object tile : tiles) {
                Logger.logDbg(getObjectField(tile, "title") + " - " + getObjectField(tile, "summary"));
            }
        }
        Logger.logDbg("########## End Categories Dump ##########");
    }

    /**
     * Modify resources on <code>TileView</code> update
     */
    private final XC_MethodHook onBindTile = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("On bind tile");

            Object dashboardItemHolder = param.args[0];
            Object tile = param.args[1];
            //Logger.logDbg("Tile summary is "+getObjectField(tile, "summary"));
            try {
                String tileName = getObjectField(tile, "title").toString();

                ImageView imageView = (ImageView) getObjectField(dashboardItemHolder, "icon");
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);

                for (int i = 0; i < serializableDashboardCategoryList.size(); i++) {
                    List<SerializableDashboardTile> tiles = serializableDashboardCategoryList.get(i).tiles;
                    for (int j = 0; j < tiles.size(); j++) {
                        if (tileName.equalsIgnoreCase(tiles.get(j).title) && Utils.notEmpty(tiles.get(j).iconPath)) {
                            imageView.setImageDrawable(Drawable.createFromPath(tiles.get(j).iconPath));
                        }
                    }
                }
            } catch (Throwable t) {
                Logger.logDbg("Field title not found");
            }
        }
    };

    /**
     * Modify resources on TileView update
     */
    private final XC_MethodHook updateTileViewHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("Updating TileView");

            Object dashboardTile = param.args[2];
            String tileName = getTitle(dashboardTile);
            ImageView imageView = (ImageView) param.args[3];
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);

            for (int i = 0; i < serializableDashboardCategoryList.size(); i++) {
                List<SerializableDashboardTile> tiles = serializableDashboardCategoryList.get(i).tiles;
                for (int j = 0; j < tiles.size(); j++) {
                    if (tileName.equalsIgnoreCase(tiles.get(j).title) && Utils.notEmpty(tiles.get(j).iconPath)) {
                        imageView.setImageDrawable(Drawable.createFromPath(tiles.get(j).iconPath));
                    }
                }
            }
        }
    };

    /**
     * Modify the dashboard according to the user modifications or save the current configuration if it isn't saved yet
     *
     * @param categories current dashboard config
     * @return modified dashboard config
     */
    @NonNull
    private List<Object> modifyDashboard(@NonNull List<Object> categories) {
        Logger.logDbg("Modifying Dashboard");

        // Launch Settings Editor
        if (IOManager.fileExists(IOManager.READ_SETTINGS_TILES)) {
            Logger.logDbg("Read settings tiles exists");

            saveCategories(categories);
            IOManager.deleteFile(IOManager.READ_SETTINGS_TILES);

            Logger.logDbg("Starting Settings Editor with package name " + BuildConfig.APPLICATION_ID);
            if (settingsActivityContext != null) {
                startSettingsEditor();
            } else {
                restart = true;
            }
        }

        serializableDashboardCategoryList = DashboardManager.getTiles();
        if (serializableDashboardCategoryList == null) {
            // Save current tiles if getTiles returned null
            Logger.logDbg("getTiles returned null, saving categories");
            serializableDashboardCategoryList = saveCategories(categories);
        }

        // Log summaries
        for (Object category : categories) {
            List<Object> tiles = (List<Object>) getObjectField(category, "tiles");
            for (Object tile : tiles) {
                Logger.logDbg("Summary of " + getObjectField(tile, "title") + " is " + getObjectField(tile, "summary"));
            }
        }

        // Clear current categories
        categories.clear();

        Logger.logDbg("Looping through categories");
        for (SerializableDashboardCategory category : serializableDashboardCategoryList) {
            Logger.logDbg("Adding " + category.title + " to list");
            Object cat = category.toDashboardCategory(DashboardCategory, DashboardTile);
            if (cat != null) {
                categories.add(cat);
            } else {
                Logger.logDbg("Category is null");
            }
        }
        Logger.logDbg("Returning list of " + categories.size() + " categories");
        return categories;
    }

    /**
     * Save categories to storage
     *
     * @param categories categories of Settings to save
     * @return serialized categories
     */
    @NonNull
    private List<SerializableDashboardCategory> saveCategories(@NonNull List<Object> categories) {
        List<SerializableDashboardCategory> serializableCategories = new ArrayList<>();
        for (Object category : categories) {
            serializableCategories.add(new SerializableDashboardCategory(category, settingsActivityContext, this));
        }
        DashboardManager.saveTiles(serializableCategories);
        return serializableCategories;
    }

    /**
     * Hook into method that triggers when a category is loaded in the main Settings menu (dashboard)
     */
    private final XC_MethodHook loadCategoriesFromResourceHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("loadCategoriesFromResourceHook");
            List<Object> categories = (List<Object>) param.args[1];
            param.args[1] = modifyDashboard(categories);
        }
    };
    /**
     * Hook into method that triggers when a category is loaded in the main Settings menu (dashboard)
     */
    private final XC_MethodHook getDashboardCategoriesHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("getDashboardCategoriesHook");
            if (settingsActivityContext == null)
                settingsActivityContext = (Context) param.thisObject;
            List<Object> categories = (List<Object>) param.getResult();
            param.setResult(modifyDashboard(categories));
        }
    };

    /**
     * Modify categories Nougat
     */
    private final XC_MethodHook getCategories = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("Modifying categories in TileUtils");
            param.setResult(modifyDashboard((List) param.getResult()));
        }
    };

    /**
     * Start settings editor after getting categories
     */
    private void startSettingsEditor() {
        if (settingsActivityContext == null) return;
        PackageManager pm = settingsActivityContext.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        if (intent != null) {
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            settingsActivityContext.startActivity(intent);
        }
        System.exit(0);
    }

    /**
     * Get title of a <code>DashboardTile</code> or <code>DashboardCategory</code>
     *
     * @param object <code>DashboardTile</code> or <code>DashboardCategory</code>
     * @return Title of <code>DashboardTile</code> or <code>DashboardCategory</code>
     */
    public String getTitle(Object object) {
        Object title;
        if (settingsActivityContext != null) {
            try {
                title = callMethod(object, "getTitle", settingsActivityContext.getResources());
                if (title != null)
                    return title.toString();
            } catch (NoSuchMethodError e) {
            }

            try {
                title = getObjectField(object, "titleRes");
                if ((int) title != 0x0)
                    return settingsActivityContext.getResources().getString((int) title);
            } catch (NoSuchFieldError e) {
            }
        }

        try {
            title = getObjectField(object, "title");
            if (title != null)
                return title.toString();
        } catch (NoSuchFieldError e) {
        }

        return "";
    }
}
