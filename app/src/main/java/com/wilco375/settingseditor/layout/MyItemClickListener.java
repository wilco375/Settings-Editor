package com.wilco375.settingseditor.layout;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Extension of {@link android.widget.AdapterView.OnItemClickListener} that also stores the index of the clicked item and its adapter
 */
public class MyItemClickListener implements AdapterView.OnItemClickListener {
    public int number;
    public ArrayAdapter adapter;

    public MyItemClickListener(int number, ArrayAdapter adapter) {
        this.number = number;
        this.adapter = adapter;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
