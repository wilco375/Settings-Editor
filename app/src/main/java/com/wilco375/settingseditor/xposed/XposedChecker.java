package com.wilco375.settingseditor.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Class to check if the module is correctly loaded and activated
 */
public class XposedChecker {
    /**
     * Checks if Xposed is active, is changed by {@link Xposed#handleLoadPackage(XC_LoadPackage.LoadPackageParam)} hook
     * @return true if active
     */
    public static boolean active(){
        return false;
    }
}
