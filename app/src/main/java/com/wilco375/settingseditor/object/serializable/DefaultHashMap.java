package com.wilco375.settingseditor.object.serializable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Object extending HashMap<K,V> that adds an get method with a default return value
 *
 * @param <K> Key
 * @param <V> Value
 */
public class DefaultHashMap<K, V> extends HashMap<K, V> implements Serializable {

    public DefaultHashMap() {
    }

    /**
     * Return defaultValue if k is not a key in HashMap<K,V>
     *
     * @param k            Key to get from HashMap<K,V>
     * @param defaultValue Default value to return if key doesn't exist
     * @return Value of key in hashmap or defaultvalue if it doesn't exist
     */
    public V get(K k, V defaultValue) {
        return containsKey(k) ? super.get(k) : defaultValue;
    }
}