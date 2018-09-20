package com.wilco375.settingseditor.general;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardCategory;
import com.wilco375.settingseditor.xposed.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

@SuppressWarnings("unchecked")
/**
 * Class to manage the storing and reading of dashboard categories
 */
public class DashboardManager {
    /**
     * Get tiles from storage
     *
     * @return List of dashboardCategories (null on error)
     */
    @Nullable
    public static List<SerializableDashboardCategory> getTiles() {
        Logger.logDbg("Getting tiles");
        if (IOManager.fileExists(IOManager.JSON_CATEGORIES)) {
            Logger.logDbg("Getting JSON tiles");
            try {
                List<SerializableDashboardCategory> list = new ObjectMapper().readValue(
                        new File(IOManager.FILEPATH, IOManager.JSON_CATEGORIES),
                        new TypeReference<ArrayList<SerializableDashboardCategory>>() {
                        }
                );
                Logger.logDbg("Found " + list.toString());
                return list;
            } catch (IOException e) {
                Logger.logDbg("IOException: " + e.toString());
                e.printStackTrace();
            }
        } else if (IOManager.fileExists(IOManager.SERIALIZED_CATEGORIES)) {
            Logger.logDbg("Getting serialized tiles");
            try {
                List<SerializableDashboardCategory> categories = (List<SerializableDashboardCategory>) IOManager.readObject(IOManager.SERIALIZED_CATEGORIES);
                // Save as JSON when saved in old format
                saveTiles(categories);
                IOManager.deleteFile(IOManager.SERIALIZED_CATEGORIES);
                return categories;
            } catch (ClassCastException e) {
                Logger.logDbg("ClassCastException");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Save tiles to storage
     *
     * @param categories List of dashboardCategories to save
     */
    public static void saveTiles(@Nullable List<SerializableDashboardCategory> categories) {
        Logger.logDbg("Saving tiles " + categories);
        if (categories == null) {
            Logger.logDbg("Tiles is null");
            return;
        }
        try {
            File file = new File(IOManager.FILEPATH, IOManager.JSON_CATEGORIES);
            Logger.logDbg("Making dirs at " + file.getParent());
            file.getParentFile().mkdirs();
            Logger.logDbg("Writing value...");
            new ObjectMapper().writeValue(file, categories);
            Logger.logDbg("Saved");
        } catch (IOException e) {
            Logger.logDbg("IOException: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Reset the changes to the dashboard by deleting all the dashboard config files
     */
    public static void reset() {
        IOManager.deleteFile(IOManager.SERIALIZED_CATEGORIES);
        IOManager.deleteFile(IOManager.JSON_CATEGORIES);
    }
}
