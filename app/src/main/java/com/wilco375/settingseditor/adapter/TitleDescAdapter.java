package com.wilco375.settingseditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.object.MainListItem;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for items to show a list of activities to launch
 */
public class TitleDescAdapter extends ArrayAdapter<MainListItem> {

    public TitleDescAdapter(Context context, List<MainListItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        MainListItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_title_desc, parent, false);
        }
        if (item == null) return convertView;

        ((TextView) convertView.findViewById(R.id.title)).setText(item.title);
        ((TextView) convertView.findViewById(R.id.desc)).setText(item.desc);

        return convertView;
    }
}
