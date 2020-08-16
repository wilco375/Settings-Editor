package com.wilco375.settingseditor.xposed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.wilco375.settingseditor.general.PreferenceConstants;
import com.wilco375.settingseditor.general.PreferencesManager;
import com.wilco375.settingseditor.general.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Xposed hooks related to changing the layout and appearance of the Settings app
 */
public class SettingsLayoutHook {

    /**
     * Hook all the necessary methods and constructors on Settings package load
     *
     * @param lpparam load package parameter
     */
    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        Logger.logDbg("Hooking methods to change layout settings");

        final PreferencesManager prefs = PreferencesManager.getInstance();

        try {
            findAndHookMethod(findClass("com.android.settings.applications.InstalledAppDetails", lpparam.classLoader), "setAppLabelAndIcon", PackageInfo.class, setAppLabelAndIcon);
        } catch (Throwable t) {
            try {
                Class<?> AppEntry = findClass("com.android.settingslib.applications.ApplicationsState.AppEntry", lpparam.classLoader);
                findAndHookMethod(findClass("com.android.settings.applications.appinfo.AppHeaderViewPreferenceController", lpparam.classLoader), "setAppLabelAndIcon", PackageInfo.class, AppEntry, setAppLabelAndIcon);
            } catch (Throwable t1) {
                Logger.logDbg("setAppLabelAndIcon not found");
            }
        }

        if (Utils.belowNougat()) {
            // Marshmallow and below

            // DashboardSummary rebuildUI Hook
            findAndHookMethod(findClass("com.android.settings.dashboard.DashboardSummary", lpparam.classLoader), "rebuildUI", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Logger.logDbg("DashboardSummary Constructor");

                    // Change background color
                    changeBackgroundColor(prefs, param);

                    // Change text color
                    changeTextColor(prefs, param);
                }
            });

            // DashboardContainerView Constructor Hook
            findAndHookConstructor(findClass("com.android.settings.dashboard.DashboardContainerView", lpparam.classLoader), Context.class, AttributeSet.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Logger.logDbg("DashboardContainerView Constructor");

                    // Change amount of columns
                    changeColumnsAmount(prefs, param);
                }
            });

            // DashboardTileView Constructor Hook
            try {
                // Hook after constructor for normal Settings
                findAndHookConstructor(findClass("com.android.settings.dashboard.DashboardTileView", lpparam.classLoader), Context.class, AttributeSet.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Logger.logDbg("DashboardTileView Constructor");

                        // Change icon size
                        changeIconSize(prefs, param);

                        // Show icons only
                        showIconsOnly(prefs, param);

                        // Change text color
                        changeTextColor(prefs, param);
                    }
                });
            } catch (NoSuchMethodError e) {
                // Hook after constructor for some Samsung Settings
                findAndHookConstructor(findClass("com.android.settings.dashboard.DashboardTileView", lpparam.classLoader), Context.class, AttributeSet.class, boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Logger.logDbg("DashboardTileView 2 Constructor");

                        // Change icon size
                        changeIconSize(prefs, param);

                        // Show icons only
                        showIconsOnly(prefs, param);

                        // Change text color
                        changeTextColor(prefs, param);
                    }
                });
            }

            // DashboardSummary updateTileView Hook
            Class<?> DashboardSummary = findClass("com.android.settings.dashboard.DashboardSummary", lpparam.classLoader);
            Class<?> SettingsActivity = findClass("com.android.settings.SettingsActivity", lpparam.classLoader);
            Class<?> DashboardTile = findClass("com.android.settings.dashboard.DashboardTile", lpparam.classLoader);
            try {
                // Stock Android
                findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, updateTileViewHook);
            } catch (Throwable t) {
                // CyanogenMod
                try {
                    findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, Switch.class, updateTileViewHook);
                } catch (Throwable t1) {
                    // Touchwiz
                    try {
                        findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, int.class, updateTileViewHook);
                        findAndHookMethod(SettingsActivity, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, int.class, updateTileViewHook);
                    } catch (Throwable t2) {
                        // Touchwiz
                        try {
                            findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, int.class, updateTileViewHook);
                            findAndHookMethod(SettingsActivity, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, int.class, updateTileViewHook);
                        } catch (Throwable t3) {
                            // Sony
                            try {
                                findAndHookMethod(DashboardSummary, "updateTileView", Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, updateTileViewHook);
                            } catch (Throwable t4) {
                                // Other method name
                                try {
                                    Method[] updateTileView = findMethodsByExactParameters(DashboardSummary, void.class, Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class);
                                    Logger.logDbg("[Settings Editor] Found " + updateTileView.length + " matches using findMethodsByExactParameters to find updateTileView");
                                    if (updateTileView.length == 1) {
                                        Logger.logDbg("[Settings Editor] Hooking method with the name " + updateTileView[0].getName());
                                        findAndHookMethod(DashboardSummary, updateTileView[0].getName(), Context.class, Resources.class, DashboardTile, ImageView.class, TextView.class, TextView.class, updateTileViewHook);
                                    }
                                } catch (Throwable t5) {
                                    XposedBridge.log("[Settings Editor] Error hooking updateTileView");
                                    XposedBridge.log(t2);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Nougat

            // Stock Android
            try {
                findAndHookConstructor(findClass("com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", lpparam.classLoader), View.class, dashboardItemHolder);
            } catch (Throwable t) {
                // Sony
                try {
                    findAndHookConstructor(findClass("com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", lpparam.classLoader), View.class, boolean.class, dashboardItemHolder);
                } catch (Throwable t1) {
                    // HTC
                    findAndHookConstructor(findClass("com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", lpparam.classLoader), "com.android.settings.dashboard.DashboardAdapter", View.class, dashboardItemHolder);
                }
            }

            try {
                findAndHookMethod(findClass("com.android.settings.dashboard.DashboardAdapter", lpparam.classLoader), "onBindSuggestionHeader", "com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", "com.android.settings.dashboard.DashboardData.SuggestionHeaderData", hideStatusText);
            } catch (Throwable t) {
                try {
                    findAndHookMethod(findClass("com.android.settings.dashboard.DashboardAdapter", lpparam.classLoader), "onBindSuggestionHeader", "com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", hideStatusText);
                } catch (Throwable t1) {
                }
            }
            findAndHookMethod(findClass("com.android.settings.dashboard.DashboardAdapter", lpparam.classLoader), "onBindTile", "com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder", "com.android.settingslib.drawer.Tile", hideStatusText);

            Class<?> DashboardAdapter = findClass("com.android.settings.dashboard.DashboardAdapter", lpparam.classLoader);
            try {
                // Nougat
                findAndHookMethod(DashboardAdapter, "setCategoriesAndSuggestions", List.class, List.class, setSuggestions);
            } catch (Throwable t) {
                try {
                    // Oreo
                    findAndHookMethod(DashboardAdapter, "setCategoriesAndSuggestions", "com.android.settingslib.drawer.DashboardCategory", List.class, setSuggestions);
                } catch (Throwable t1) {
                    Logger.logDbg("setCategoriesAndSuggestions not found");
                }
            }

            try {
                // Nougat
                findAndHookMethod(DashboardAdapter, "setSuggestions", List.class, setSuggestions);
            } catch (Throwable t) {
                Logger.logDbg("setSuggestions not found");
            }
        }
    }

    private static final XC_MethodHook setAppLabelAndIcon = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("setAppLabelAndIcon");

            PreferencesManager prefs = PreferencesManager.getInstance();

            // Set installed app icon onClickListener
            setInstalledAppIconListener(prefs, param);

            // Show package name
            showPackageName(prefs, param);
        }
    };

    /**
     * Remove suggestions
     */
    private static final XC_MethodHook setSuggestions = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            PreferencesManager prefs = PreferencesManager.getInstance();
            if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_HIDE_SUGGESTIONS, false)) {
                if (param.args.length > 1) {
                    param.args[1] = new ArrayList<>();
                } else {
                    param.args[0] = new ArrayList<>();
                }
            }
        }
    };

    /**
     * Nougat modify settings layout and appearance
     */
    private static final XC_MethodHook dashboardItemHolder = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("DashboardAdapter.DashboardItemHolder Constructor");

            PreferencesManager prefs = PreferencesManager.getInstance();

            ImageView imageView = (ImageView) getObjectField(param.thisObject, "icon");
            if (imageView != null) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
            }

            // Change background color
            changeBackgroundColor(prefs, param);

            // Change text color
            changeTextColor(prefs, param);

            // Change icon size
            changeIconSize(prefs, param);

            // Show icons only
            showIconsOnly(prefs, param);

            // Remove icon background
            removeIconBackground(prefs, param);

            // Set icon filter color
            setIconFilterColor(prefs, param);

            // Hide status text
            hideStatusText(prefs, param);
        }
    };

    /**
     * Nougat hide status text
     */
    private static final XC_MethodHook hideStatusText = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

            PreferencesManager prefs = PreferencesManager.getInstance();

            if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_HIDE_STATUS, false)) {
                Logger.logDbg("Hiding status text");
                Object holder = param.args[0];
                View statusText = (View) getObjectField(holder, "summary");
                statusText.setVisibility(View.GONE);
            }
        }
    };

    /**
     * DashboardSummary updateTileView Hook
     */
    private static final XC_MethodHook updateTileViewHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Logger.logDbg("Updating tileView in SettingsLayoutHook");

            PreferencesManager prefs = PreferencesManager.getInstance();

            if (param.args[3] instanceof ImageView) {
                // Remove icon background
                removeIconBackground(prefs, param);

                // Set icon filter color
                setIconFilterColor(prefs, param);
            }

            if (param.args[5] != null && param.args[5] instanceof TextView) {
                // Hide status text
                hideStatusText(prefs, param);
            }

            super.afterHookedMethod(param);
        }
    };

    /**
     * Hide status text
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void hideStatusText(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_HIDE_STATUS, false)) {
            Logger.logDbg("Hiding status text in updateTileView");

            try {
                TextView status;
                if (Utils.belowNougat()) {
                    status = (TextView) param.args[5];
                } else {
                    status = (TextView) getObjectField(param.thisObject, "summary");
                }
                if (status != null) {
                    status.setVisibility(View.GONE);
                    Logger.logDbg("Status text view with text '" + status.getText().toString() + "' set to invisible");
                }
            } catch (NoSuchFieldError e) {
                Logger.logDbg("Status text view not found");
                e.printStackTrace();
            }
        }
    }

    /**
     * Show icons only on Settings dashboard
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void showIconsOnly(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_ICONS_ONLY, false)) {
            Logger.logDbg("Showing icons only");

            // Change icon ImageView width to parent width
            try {
                String iconField = Utils.belowNougat() ? "mImageView" : "icon";
                ImageView icon = (ImageView) getObjectField(param.thisObject, iconField);
                if (icon != null) {
                    // Linear layout parent
                    try {
                        LinearLayout.LayoutParams iconLayoutParams = ((LinearLayout.LayoutParams) icon.getLayoutParams());
                        if (iconLayoutParams == null)
                            iconLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        else
                            iconLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;

                        // Make sure opposite margins are equal so that the icon is centered
                        if (Utils.aboveJellybean()) {
                            iconLayoutParams.setMarginEnd(iconLayoutParams.getMarginStart());
                        } else {
                            iconLayoutParams.setMargins(iconLayoutParams.leftMargin, iconLayoutParams.topMargin,
                                    iconLayoutParams.leftMargin, iconLayoutParams.topMargin);
                        }
                        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        icon.setAdjustViewBounds(true);

                        Logger.logDbg("-- Applied to LinearLayout");
                    }

                    // Relative layout parent
                    catch (ClassCastException e) {
                        RelativeLayout.LayoutParams iconLayoutParams = ((RelativeLayout.LayoutParams) icon.getLayoutParams());
                        if (iconLayoutParams == null)
                            iconLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        else
                            iconLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;

                        // Make sure opposite margins are equal so that the icon is centered
                        if (Utils.aboveJellybean()) {
                            iconLayoutParams.setMarginEnd(iconLayoutParams.getMarginStart());
                        } else {
                            iconLayoutParams.setMargins(iconLayoutParams.leftMargin, iconLayoutParams.topMargin,
                                    iconLayoutParams.leftMargin, iconLayoutParams.topMargin);
                        }
                        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        icon.setAdjustViewBounds(true);

                        Logger.logDbg("-- Applied to RelativeLayout");
                    }
                }
            } catch (NoSuchFieldError e) {
                Logger.log("Error while changing to icons only: field not found");
                e.printStackTrace();
            }

            // Make all the other views invisible
            try {
                TextView title = (TextView) getObjectField(param.thisObject, "mTitleTextView");
                if (title != null)
                    title.setVisibility(View.INVISIBLE);
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            try {
                TextView status = (TextView) getObjectField(param.thisObject, "mStatusTextView");
                if (status != null)
                    status.setVisibility(View.INVISIBLE);
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            try {
                View divider = (View) getObjectField(param.thisObject, "mDivider");
                if (divider != null)
                    divider.setVisibility(View.INVISIBLE);
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            try {
                Switch aSwitch = (Switch) getObjectField(param.thisObject, "mSwitch");
                if (aSwitch != null)
                    aSwitch.setVisibility(View.INVISIBLE);
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove icon background
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void removeIconBackground(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (Utils.aboveJellybean() && prefs.getBoolean(PreferenceConstants.KEY_BOOL_REMOVE_BACKGROUND, false)) {
            Logger.logDbg("Removing icon background");

            ImageView imageView;
            if (Utils.belowNougat()) {
                imageView = (ImageView) param.args[3];
            } else {
                imageView = (ImageView) getObjectField(param.thisObject, "icon");
            }
            imageView.setBackground(null);
        }
    }

    /**
     * Set icon filter color
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void setIconFilterColor(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        ImageView imageView;
        if (Utils.belowNougat()) {
            imageView = (ImageView) param.args[3];
        } else {
            imageView = (ImageView) getObjectField(param.thisObject, "icon");
        }

        if (imageView != null) {
            if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_FILTER_COLOR, false)) {
                Logger.logDbg("Adding color filter to icon");

                String overlayColor = prefs.getString(PreferenceConstants.KEY_STRING_FILTER_COLOR, "#000000");
                if (overlayColor.length() == 6) overlayColor = "#" + overlayColor;
                imageView.setColorFilter(
                        Color.parseColor(overlayColor),
                        PorterDuff.Mode.SRC_IN);
            } else {
                imageView.clearColorFilter();
            }
        }
    }

    /**
     * Change the default icon size on the Settings dashboard
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void changeIconSize(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_ICON_SIZE, false)) {
            Logger.logDbg("Changing icon size");

            try {
                String iconField = Utils.belowNougat() ? "mImageView" : "icon";
                ImageView icon = (ImageView) getObjectField(param.thisObject, iconField);
                if (icon != null) {
                    try {
                        //Linear layout
                        LinearLayout.LayoutParams originalLayoutPrams = (LinearLayout.LayoutParams) icon.getLayoutParams();
                        LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(
                                prefs.getInteger(PreferenceConstants.KEY_INT_ICON_SIZE, 100),
                                prefs.getInteger(PreferenceConstants.KEY_INT_ICON_SIZE, 100));
                        if (Utils.aboveJellybean()) {
                            iconLayoutParams.setMarginEnd(originalLayoutPrams.getMarginEnd());
                            iconLayoutParams.setMarginStart(originalLayoutPrams.getMarginStart());
                        } else {
                            // left top right bottom
                            iconLayoutParams.setMargins(originalLayoutPrams.leftMargin, 0, originalLayoutPrams.rightMargin, 0);
                        }
                        icon.setLayoutParams(iconLayoutParams);

                        Logger.logDbg("-- Applied to LinearLayout");
                    } catch (ClassCastException e) {
                        //Relative layout
                        RelativeLayout.LayoutParams originalLayoutPrams = (RelativeLayout.LayoutParams) icon.getLayoutParams();
                        RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(
                                prefs.getInteger(PreferenceConstants.KEY_INT_ICON_SIZE, 100),
                                prefs.getInteger(PreferenceConstants.KEY_INT_ICON_SIZE, 100));
                        if (Utils.aboveJellybean()) {
                            iconLayoutParams.setMarginEnd(originalLayoutPrams.getMarginEnd());
                            iconLayoutParams.setMarginStart(originalLayoutPrams.getMarginStart());
                        } else {
                            // left top right bottom
                            iconLayoutParams.setMargins(originalLayoutPrams.leftMargin, 0, originalLayoutPrams.rightMargin, 0);
                        }
                        icon.setLayoutParams(iconLayoutParams);

                        Logger.logDbg("-- Applied to RelativeLayout");
                    }
                }
            } catch (NoSuchFieldError e) {
                Logger.log("Error while editing icon size: field not found");
                e.printStackTrace();
            }
        }
    }

    /**
     * Change amount of columns on the Settings dashboard
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void changeColumnsAmount(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_COLUMN_COUNT, false)) {
            Logger.logDbg("Changing amount of columns");

            setObjectField(param.thisObject, "mNumColumns", prefs.get(PreferenceConstants.KEY_INT_COLUMN_COUNT, 1));
        }
    }

    /**
     * Change the background color of the dashboard
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void changeBackgroundColor(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_BACKGROUND, false)) {

            Logger.logDbg("Changing background color");

            if (Utils.belowNougat()) {
                try {
                    ViewGroup dashboardContainerView = (ViewGroup) getObjectField(param.thisObject, "mDashboard");
                    int bgColor = Color.parseColor(prefs.getString(PreferenceConstants.KEY_STRING_BACKGROUND, "#FFFFFF"));
                    dashboardContainerView.setBackgroundColor(bgColor);
                    for (int i = 0; i < dashboardContainerView.getChildCount(); i++) {
                        dashboardContainerView.getChildAt(i).setBackgroundColor(bgColor);
                    }
                } catch (NoSuchFieldError e) {
                    XposedBridge.log("[Settings Editor] Error while changing background color: field not found");
                    e.printStackTrace();
                }
            } else {
                View itemView = (View) param.args[0];
                if (itemView != null) {
                    int bgColor = Color.parseColor(prefs.getString(PreferenceConstants.KEY_STRING_BACKGROUND, "#FFFFFF"));
                    itemView.setBackgroundColor(bgColor);
                }
            }
        }
    }

    /**
     * Add {@link android.view.View.OnClickListener} to installed app icon to launch that app
     *
     * @param prefs preferences
     * @param param method hook parameter
     */
    private static void setInstalledAppIconListener(PreferencesManager prefs, final XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_INSTALLED_APP_ICON, false)) {
            Logger.logDbg("Setting installed apps icon onClickListener");

            View appSnippet = null;
            try {
                final Context context = (Context) callMethod(param.thisObject, "getContext");
                appSnippet = (View) callMethod(getObjectField(param.thisObject, "mHeader"), "findViewById", context.getResources().getIdentifier("app_snippet", "id", "com.android.settings"));
            } catch (Throwable t) {
                try {
                    View rootView = (View) getObjectField(param.thisObject, "mRootView");
                    appSnippet = rootView.findViewById(rootView.getResources().getIdentifier("app_snippet", "id", "com.android.settings"));
                } catch (Throwable t1) {
                    Logger.logDbg("appSnippet not found");
                }
            }
            ImageView icon = appSnippet == null ? null : appSnippet.findViewById(android.R.id.icon);
            if (icon == null) {
                Logger.logDbg("Finding icon in mRootView");
                View rootView = (View) getObjectField(getObjectField(param.thisObject, "mHeader"), "mRootView");
                icon = rootView.findViewById(rootView.getResources().getIdentifier("entity_header_icon", "id", "com.android.settings"));
            }

            if (icon != null) {
                final Context finalContext =  icon.getContext();
                icon.setOnClickListener(v -> {
                    PackageManager pm = finalContext.getPackageManager();
                    Intent i = pm.getLaunchIntentForPackage(((PackageInfo) param.args[0]).packageName);
                    if (i != null) {
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        finalContext.startActivity(i);
                    }
                });
            } else {
                Logger.logDbg("Icon not found");
            }
        }
    }

    /**
     * Show package name in the InstalledAppDetails Activity
     *
     * @param prefs preferences
     * @param param method hook parameters
     */
    private static void showPackageName(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_SHOW_PACKAGE, false)) {
            Logger.logDbg("Showing package name");

            TextView text = null;
            String packageName = null;
            if (Utils.belowOreo()) {
                View appSnippet;
                try {
                    final Context context = (Context) callMethod(param.thisObject, "getContext");
                    appSnippet = (View) callMethod(getObjectField(param.thisObject, "mHeader"), "findViewById", context.getResources().getIdentifier("app_snippet", "id", "com.android.settings"));
                } catch (Throwable t) {
                    appSnippet = ((View) getObjectField(param.thisObject, "mRootView")).findViewById(((View) getObjectField(param.thisObject, "mRootView")).getResources().getIdentifier("app_snippet", "id", "com.android.settings"));
                }
                text = appSnippet.findViewById(appSnippet.getResources().getIdentifier("widget_text1", "id", "com.android.settings"));
                packageName = ((PackageInfo) param.args[0]).packageName;
            } else {
                // Oreo
                try {
                    View rootView = (View) getObjectField(getObjectField(param.thisObject, "mHeader"), "mRootView");
                    text = rootView.findViewById(rootView.getResources().getIdentifier("entity_header_second_summary", "id", "com.android.settings"));
                    packageName = (String) getObjectField(param.thisObject, "mPackageName");
                } catch (Throwable t) {
                    t.printStackTrace();
                    Logger.logDbg("Could not find root view");
                }
            }

            if (text != null) {
                Logger.logDbg("Adding package name "+packageName+" to text");

                String currentText = text.getText().toString();
                if (!currentText.isEmpty()) {
                    text.setText(currentText + " - " + packageName);
                } else {
                    text.setText(packageName);
                }
            } else {
                Logger.logDbg("Could not find text");
            }
        }
    }

    /**
     * Change the text color
     *
     * @param prefs preferences
     * @param param method hook parameters
     */
    private static void changeTextColor(PreferencesManager prefs, XC_MethodHook.MethodHookParam param) {
        if (prefs.getBoolean(PreferenceConstants.KEY_BOOL_TEXT_COLOR, false)) {
            Logger.logDbg("Changing text color");

            int textColor = Color.parseColor(prefs.getString(PreferenceConstants.KEY_STRING_TEXT_COLOR, "#000000"));

            try {
                String titleField = Utils.belowNougat() ? "mTitleTextView" : "title";
                TextView title = (TextView) getObjectField(param.thisObject, titleField);
                if (title != null)
                    title.setTextColor(textColor);
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            try {
                String statusField = Utils.belowNougat() ? "mStatusTextView" : "summary";
                TextView status = (TextView) getObjectField(param.thisObject, statusField);
                if (status != null)
                    status.setTextColor(textColor);
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            if (Utils.belowOreo()) {
                try {
                    ViewGroup dashboardContainerView = (ViewGroup) getObjectField(param.thisObject, "mDashboard");
                    Context context = (Context) callMethod(param.thisObject, "getContext");
                    for (int i = 0; i < dashboardContainerView.getChildCount(); i++) {
                        TextView categoryTitle = dashboardContainerView.getChildAt(i).findViewById(
                                context.getResources().getIdentifier("category_title", "id", "com.android.settings")
                        );
                        if (categoryTitle != null)
                            categoryTitle.setTextColor(textColor);
                    }
                } catch (NoSuchFieldError e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

