package com.wilco375.settingseditor.xposed;

import com.wilco375.settingseditor.BuildConfig;
import com.wilco375.settingseditor.general.PreferenceConstants;
import com.wilco375.settingseditor.general.PreferencesManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Xposed init class
 */
public class Xposed implements IXposedHookLoadPackage {

    /**
     * Called by Xposed
     * Modify settings and set {@link XposedChecker#active()} method to return true
     */
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // Set Xposed active to true
        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedHelpers.findAndHookMethod("com.wilco375.settingseditor.xposed.XposedChecker", lpparam.classLoader, "active", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
            Logger.logDbg("Set Xposed Active to true");
        }

        // Return if package is not Settings
        if (!lpparam.packageName.equals("com.android.settings"))
            return;

        // Enable debug logging if debug preference is set
        PreferencesManager prefs = PreferencesManager.getInstance();
        Logger.debug = prefs.getBoolean(PreferenceConstants.KEY_BOOL_DEBUG, false);

        // Modify dashboardItems
        DashboardHook dashboardHook = new DashboardHook();
        dashboardHook.handleLoadPackage(lpparam);

        // Layout preferences
        SettingsLayoutHook.handleLoadPackage(lpparam);
    }
}
