package com.wilco375.settingseditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wilco375.settingseditor.R;

import java.util.List;

/**
 * Adapter for sortable String array, used in CategoriesActivity
 */
public class SortableStringAdapter extends ArrayAdapter<String> {
    public SortableStringAdapter(Context context, List<String> text) {
        super(context, 0, text);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String text = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_tile, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.drag_handle);
        TextView title = convertView.findViewById(R.id.listViewTitle);

        icon.setImageResource(R.drawable.drag);
        title.setText(text);

        return convertView;
    }
}
