package com.wilco375.settingseditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.object.AppItem;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for AppItem to show a list of apps
 */
public class AppItemAdapter extends ArrayAdapter<AppItem> {

    public AppItemAdapter(Context context, List<AppItem> appItems) {
        super(context, 0, appItems);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        AppItem appItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_resolve_info, parent, false);
        }
        if (appItem == null) return convertView;

        ImageView icon = convertView.findViewById(R.id.adapterIcon);
        TextView title = convertView.findViewById(R.id.adapterTitle);

        icon.setImageDrawable(appItem.drawable);
        title.setText(appItem.title);

        return convertView;
    }
}
