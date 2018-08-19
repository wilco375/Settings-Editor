package com.wilco375.settingseditor.object.serializable;

import android.os.Bundle;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Serializable version of {@link Bundle}
 */
public class SerializableBundle implements Serializable {
    /**
     * List of the bundles objects
     */
    public HashMap<String, Object> map;

    /**
     * Constructor for Jackson
     */
    public SerializableBundle() {
    }

    /**
     * Create a serializable bundle from a {@link Bundle}
     *
     * @param bundle bundle to convert
     */
    public SerializableBundle(Bundle bundle) {
        if (bundle == null) return;
        map = new HashMap<>();
        for (String key : bundle.keySet()) {
            map.put(key, bundle.get(key));
        }
    }

    /**
     * Convert the serializable bundle back to a {@link Bundle}
     *
     * @return the converted bundle
     */
    public Bundle toBundle() {
        if (map == null || map.size() == 0) return null;
        Bundle bundle = new Bundle();
        for (String key : map.keySet()) {
            Object o = map.get(key);
            // Check all possible Object versions that Bundle supports
            if (o instanceof String) bundle.putString(key, (String) o);
            else if (o instanceof Integer) bundle.putInt(key, (Integer) o);
            else if (o instanceof Boolean) bundle.putBoolean(key, (Boolean) o);
            else if (o instanceof Float) bundle.putFloat(key, (Float) o);
            else if (o instanceof Long) bundle.putLong(key, (Long) o);
            else if (o instanceof Double) bundle.putDouble(key, (Double) o);
            else if (o instanceof Byte) bundle.putByte(key, (Byte) o);
            else if (o instanceof Character) bundle.putChar(key, (Character) o);
            else if (o instanceof Short) bundle.putShort(key, (Short) o);
        }
        return bundle;
    }
}
