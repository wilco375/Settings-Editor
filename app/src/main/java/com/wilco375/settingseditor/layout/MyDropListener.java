package com.wilco375.settingseditor.layout;

import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortListView;

/**
 * Extension of {@link com.mobeta.android.dslv.DragSortListView.DropListener} that also stores the original index of the dropped item and its adapter
 */
public class MyDropListener implements DragSortListView.DropListener {
    public int number;
    public ArrayAdapter adapter;

    public MyDropListener(int number, ArrayAdapter adapter) {
        this.number = number;
        this.adapter = adapter;
    }

    @Override
    public void drop(int from, int to) {

    }
}
