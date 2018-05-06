package com.wilco375.settingseditor.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.general.Utils;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardTile;

import java.util.List;

/**
 * Adapter for SerializableDashboardTile to show a list of tiles
 */
public class SerializableDashboardTileAdapter extends ArrayAdapter<SerializableDashboardTile> {
    Resources settingsRes;

    public SerializableDashboardTileAdapter(Context context, List<SerializableDashboardTile> dashboardTiles) {
        super(context, 0, dashboardTiles);
        try {
            this.settingsRes = context.getPackageManager().getResourcesForApplication("com.android.settings");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SerializableDashboardTile dashboardTile = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_tile, parent, false);
        }
        if(dashboardTile == null) return convertView;

        ImageView icon = convertView.findViewById(R.id.drag_handle);
        TextView title = convertView.findViewById(R.id.listViewTitle);

        if (Utils.notEmpty(dashboardTile.iconPath)) icon.setImageDrawable(Drawable.createFromPath(dashboardTile.iconPath));
        else if (dashboardTile.iconRes != 0x0){
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) icon.setImageDrawable(settingsRes.getDrawable(dashboardTile.iconRes, null));
                else icon.setImageDrawable(settingsRes.getDrawable(dashboardTile.iconRes));
            } catch (Resources.NotFoundException e) { }
        } else if (dashboardTile.icon != null){
            icon.setImageBitmap(dashboardTile.icon.toBitmap(getContext()));
        }
        title.setText(dashboardTile.title);

        return convertView;
    }
}
