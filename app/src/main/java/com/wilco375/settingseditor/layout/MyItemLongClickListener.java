package com.wilco375.settingseditor.layout;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Extension of {@link android.widget.AdapterView.OnItemLongClickListener} that also stores the index of the clicked item and its adapter
 */
public class MyItemLongClickListener implements AdapterView.OnItemLongClickListener {
    public int number;
    public ArrayAdapter adapter;

    public MyItemLongClickListener(int number, ArrayAdapter adapter) {
        this.number = number;
        this.adapter = adapter;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
