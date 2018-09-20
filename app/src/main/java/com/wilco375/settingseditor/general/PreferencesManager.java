package com.wilco375.settingseditor.general;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wilco375.settingseditor.object.serializable.DefaultHashMap;
import com.wilco375.settingseditor.xposed.Logger;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;

/**
 * Alternative to SharedPreferences that does not seem to be working correctly in Android 6.0 with Xposed
 */
@SuppressWarnings("unchecked")
public class PreferencesManager {
    /**
     * Hashmap that stores all the preferences
     */
    private static DefaultHashMap<Integer, Object> prefs;
    /**
     * Static {@link PreferencesManager} instance
     */
    private static PreferencesManager preferencesManager;

    /**
     * Create new {@link PreferencesManager}
     * Also gets the preferences from the local storage
     */
    public PreferencesManager() {
        Logger.logDbg("PreferencesManager Constructor");
        prefs = getPreferences();

        if (prefs == null) {
            Logger.logDbg("Prefs is null");
            prefs = new DefaultHashMap<>();
        } else Logger.logDbg("Prefs is not null");
    }

    /**
     * Returns an instance of {@link PreferencesManager}
     *
     * @return the {@link PreferencesManager} instance
     */
    public static PreferencesManager getInstance() {
        if (preferencesManager == null)
            preferencesManager = new PreferencesManager();

        return preferencesManager;
    }

    /**
     * Get the preferences from the local storage
     *
     * @return preferences hashmap
     */
    @Nullable
    private DefaultHashMap<Integer, Object> getPreferences() {
        Logger.logDbg("Getting preferences");

        if (IOManager.fileExists(IOManager.JSON_PREFS)) {
            Logger.logDbg("Getting JSON Preferences");
            try {
                return new ObjectMapper().readValue(
                        new File(IOManager.FILEPATH, IOManager.JSON_PREFS),
                        new TypeReference<DefaultHashMap<Integer, Object>>() {
                        }
                );
            } catch (IOException e) {
                Logger.logDbg("IOException");
                e.printStackTrace();
                return null;
            }
        } else if (IOManager.fileExists(IOManager.PREFS)) {
            Logger.logDbg("Getting serialized preferences");
            try {
                DefaultHashMap<Integer, Object> prefs = (DefaultHashMap<Integer, Object>) IOManager.readObject(IOManager.PREFS);
                // Save as JSON when saved in old format
                savePreferences(prefs);
                IOManager.deleteFile(IOManager.PREFS);
                return prefs;
            } catch (ClassCastException e) {
                Logger.logDbg("ClassCastException");
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Save the edited preferences
     *
     * @param prefs hashmap to save
     */
    private void savePreferences(DefaultHashMap<Integer, Object> prefs) {
        Logger.logDbg("Saving preferences");
        if (prefs == null) {
            Logger.logDbg("Preferences is null");
            return;
        }
        try {
            File file = new File(IOManager.FILEPATH, IOManager.JSON_PREFS);
            file.getParentFile().mkdirs();
            new ObjectMapper().writeValue(file, prefs);
            Logger.logDbg("Saved");
        } catch (IOException e) {
            Logger.logDbg("IOException");
            e.printStackTrace();
        }

    }

    /**
     * Update the preferences
     *
     * @return this
     */
    public PreferencesManager refresh() {
        prefs = getPreferences();
        if (prefs == null)
            prefs = new DefaultHashMap<>();

        return this;
    }

    /**
     * Get a string field value
     *
     * @param key        key of the field
     * @param defaultVal value to return if the key doesn't exist
     * @return string value
     */
    public String getString(int key, String defaultVal) {
        return (String) prefs.get(key, defaultVal);
    }

    /**
     * Get a boolean field value
     *
     * @param key        key of the field
     * @param defaultVal value to return if the key doesn't exist
     * @return boolean value
     */
    public boolean getBoolean(int key, boolean defaultVal) {
        return (boolean) prefs.get(key, defaultVal);
    }

    /**
     * Get an integer field value
     *
     * @param key        key of the field
     * @param defaultVal value to return if the key doesn't exist
     * @return integer value
     */
    public int getInteger(int key, int defaultVal) {
        return (int) prefs.get(key, defaultVal);
    }

    /**
     * Get an object field value
     *
     * @param key        key of the field
     * @param defaultVal value to return if the key doesn't exist
     * @return object
     */
    public Object get(int key, Object defaultVal) {
        return prefs.get(key, defaultVal);
    }

    /**
     * Add a field to the preferences or edit it, and immediately save the changes to local storage
     *
     * @param key   key to add/edit
     * @param value value to assign to the key
     */
    public void putAndApply(int key, Object value) {
        prefs.put(key, value);
        savePreferences(prefs);
    }

    /**
     * Add a field to the preferences or edit it
     *
     * @param key   key to add/edit
     * @param value value to assign to the key
     * @return this
     */
    public PreferencesManager put(int key, Object value) {
        prefs.put(key, value);
        return this;
    }

    /**
     * Save the preferences
     */
    public void apply() {
        savePreferences(prefs);
    }
}
