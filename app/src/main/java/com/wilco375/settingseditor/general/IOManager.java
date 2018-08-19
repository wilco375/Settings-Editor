package com.wilco375.settingseditor.general;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class IOManager {

    /**
     * Filename for file that indicates a read settings tiles from storage
     */
    public static final String READ_SETTINGS_TILES = "readSettingsTiles";

    // Categories files
    /**
     * Filename for file that contains the dashboard categories using serialization
     */
    @Deprecated
    public static final String SERIALIZED_CATEGORIES = "serializedDashboardCategories.ser";
    /**
     * Filename for file that contains the dashboard categories using json
     */
    public static final String JSON_CATEGORIES = "categories.json";

    /**
     * File path to save all app related files (usually /sdcard/data/com.wilco375/settingseditor/)
     */
    public static final String FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/com.wilco375.settingseditor/";

    // Config files
    /**
     * Filename for file that contains the preferences using serialization
     */
    @Deprecated
    public static final String PREFS = "prefs.hashmap";
    /**
     * Filename for file that contains the preferences using json
     */
    public static final String JSON_PREFS = "prefs.json";

    /**
     * Write Object to specified file using serialization
     *
     * @param obj      Object to write
     * @param filePath Path to file to write to
     * @param name     Name of file to write to
     */
    public static void writeObject(Object obj, String filePath, String name) {
        try {
            new File(filePath).mkdirs();

            // Create .nomedia file
            if (!(new File(filePath, ".nomedia").exists()))
                new File(filePath, ".nomedia").createNewFile();

            File file = new File(filePath + name);
            if (!file.exists() || file.delete()) {
                if (file.createNewFile()) {
                    file.setReadable(true, false);
                    if (obj instanceof Serializable) {
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(obj);
                        oos.close();
                    }
                }
            } else {
                System.out.println("File exists and can't be removed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write an object to the specified file using serialization to the default directory
     *
     * @param object object to write
     * @param name   filename of file to write to
     */
    public static void writeObject(Object object, String name) {
        writeObject(object, FILEPATH, name);
    }

    /**
     * Save drawable to storage as png
     *
     * @param drawable drawable to save
     * @param filePath Path to file to write to
     * @param name     Name of file to write to
     * @return destination written to
     */
    public static String writeDrawable(Drawable drawable, String filePath, String name) {
        try {
            Bitmap image = Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), 192, 192, false);
            File destination = new File(filePath, name);
            if (destination.createNewFile()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(destination);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                destination.setReadable(true, true);
                return destination.getPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Save drawable to storage as png to the default directory
     *
     * @param drawable drawable to save
     * @param name     Name of file to write to
     * @return destination written to
     */
    public static String writeDrawable(Drawable drawable, String name) {
        return writeDrawable(drawable, FILEPATH, name);
    }

    /**
     * Read Object from specified file
     *
     * @param filePath Path to file to read from
     * @param name     Name of file to read from
     * @return Read object or new Object() if non existing file
     */
    public static Object readObject(String filePath, String name) {
        try {
            File file = new File(filePath, name);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read Object from specified file in default directory
     *
     * @param name Name of file to read from
     * @return Read object or new Object() if non existing file
     */
    public static Object readObject(String name) {
        return readObject(FILEPATH, name);
    }

    /**
     * Check if specified file exists
     *
     * @param filePath Path to file to check
     * @param name     Name of file to check
     * @return File exists
     */
    public static boolean fileExists(String filePath, String name) {
        File file = new File(filePath, name);
        return file.exists();
    }

    /**
     * Check if specified file exists in default directory
     *
     * @param name Name of file to check
     * @return File exists
     */
    public static boolean fileExists(String name) {
        return fileExists(FILEPATH, name);
    }

    /**
     * Delete specified file
     *
     * @param filePath Path to file to delete
     * @param name     Name of file to delete
     */
    public static void deleteFile(String filePath, String name) {
        File file = new File(filePath, name);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Delete specified file in default directory
     *
     * @param name Name of file to delete
     */
    public static void deleteFile(String name) {
        deleteFile(FILEPATH, name);
    }
}
