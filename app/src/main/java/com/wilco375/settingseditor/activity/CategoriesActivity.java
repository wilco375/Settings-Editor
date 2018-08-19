package com.wilco375.settingseditor.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.mobeta.android.dslv.DragSortListView;
import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.adapter.SortableStringAdapter;
import com.wilco375.settingseditor.general.DashboardManager;
import com.wilco375.settingseditor.general.PreferenceConstants;
import com.wilco375.settingseditor.general.PreferencesManager;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends BaseActivity {

    SortableStringAdapter categoriesListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        MainActivity.checkIntro(this);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title of toolbar and add back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.categories);

        // Create array of category titles
        final List<String> categoryTitles = new ArrayList<>();
        for (SerializableDashboardCategory category : categories) {
            categoryTitles.add(category.title);
        }

        // Create dslv to order categories
        final DragSortListView categoriesListView = findViewById(R.id.categoriesListView);
        assert categoriesListView != null;
        categoriesListView.setDragEnabled(true);
        categoriesListViewAdapter = new SortableStringAdapter(this, categoryTitles);
        categoriesListView.setAdapter(categoriesListViewAdapter);
        categoriesListView.setDropListener((from, to) -> {
            if (!checkModifications()) {
                SerializableDashboardCategory movedCategory = categories.get(from);
                categories.remove(from);
                categoryTitles.remove(from);
                if (from > to) --from;
                PreferencesManager pm = PreferencesManager.getInstance();
                pm.putAndApply(PreferenceConstants.KEY_MODIFICATIONS,
                        pm.getInteger(PreferenceConstants.KEY_MODIFICATIONS, 0) + 1);
                categories.add(to, movedCategory);
                categoryTitles.add(to, movedCategory.title);
                saveTiles();
                categoriesListViewAdapter.notifyDataSetChanged();
            }
        });
        categoriesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            // Remove category on long click
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.remove_category);
            builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                categories.remove(position);
                categoryTitles.remove(position);
                saveTiles();
                categoriesListViewAdapter.notifyDataSetChanged();
            });
            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
            });
            builder.create().show();
            return true;
        });
        categoriesListView.setOnItemClickListener((parent, view, position, id) -> {
            // Rename category on click
            final AlertDialog.Builder modifyCategoryDialog = new AlertDialog.Builder(activity);
            modifyCategoryDialog.setCancelable(false);
            View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_modify_category, null);
            final EditText titleEditText = dialogView.findViewById(R.id.title);
            titleEditText.setText(categories.get(position).title);
            modifyCategoryDialog.setView(dialogView);
            modifyCategoryDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                categories.get(position).title = titleEditText.getText().toString();
                categoryTitles.set(position, titleEditText.getText().toString());
                saveTiles();
                categoriesListViewAdapter.notifyDataSetChanged();
            });
            modifyCategoryDialog.create().show();
        });
    }

    /**
     * Options menu inflater
     *
     * @param menu Menu to be inflated
     * @return success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_categories, menu);
        return true;
    }

    /**
     * On option in menu selected
     *
     * @param item Item selected
     * @return success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (R.id.settings):
                // Kill and restart settings
                ActivityManager am = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
                if (am != null)
                    am.killBackgroundProcesses("com.android.settings");

                PackageManager pm = activity.getPackageManager();
                Intent i = pm.getLaunchIntentForPackage("com.android.settings");
                if (i != null) {
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    activity.startActivity(i);
                }
                return true;
            case (R.id.reset):
                // Delete all modifications
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.reset);
                builder.setMessage(R.string.reset_text);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i12) -> {
                    DashboardManager.reset();
                    PreferencesManager.getInstance().putAndApply(PreferenceConstants.KEY_MODIFICATIONS, 0);
                    getTiles();
                    categoriesListViewAdapter.notifyDataSetChanged();
                });
                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i1) -> {
                });
                builder.create().show();
                return true;
            case (android.R.id.home):
                // call onBackPressed() on ActionBar back button pressed
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * On device or ActionBar back button pressed, close activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.hold, R.anim.right_slide_out);
    }
}
