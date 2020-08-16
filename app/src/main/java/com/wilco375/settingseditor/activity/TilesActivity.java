package com.wilco375.settingseditor.activity;

import android.animation.Animator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AndroidException;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobeta.android.dslv.DragSortListView;
import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.adapter.AppItemAdapter;
import com.wilco375.settingseditor.adapter.SerializableDashboardTileAdapter;
import com.wilco375.settingseditor.general.DashboardManager;
import com.wilco375.settingseditor.general.IOManager;
import com.wilco375.settingseditor.general.PreferenceConstants;
import com.wilco375.settingseditor.general.PreferencesManager;
import com.wilco375.settingseditor.general.Utils;
import com.wilco375.settingseditor.layout.GalleryView;
import com.wilco375.settingseditor.layout.MyDropListener;
import com.wilco375.settingseditor.layout.MyItemClickListener;
import com.wilco375.settingseditor.layout.MyItemLongClickListener;
import com.wilco375.settingseditor.layout.MyRelativeLayout;
import com.wilco375.settingseditor.object.AppItem;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardCategory;
import com.wilco375.settingseditor.object.serializable.SerializableDashboardTile;
import com.wilco375.settingseditor.object.serializable.SerializableIntent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TilesActivity extends BaseActivity {

    // Icon of tile
    String iconPath = "";
    ImageView iconImageView;
    TextView iconTextView;

    // List of all installed apps
    List<AppItem> appsList = new ArrayList<>();
    // List of all fragments
    List<String> listOfSettingsFragments = new ArrayList<>();

    // Location to open and close the circular reveal to
    int tilesItemX;
    int tilesItemY;
    boolean fabPressed;

    // Last touched location
    int touchX;
    int touchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiles);

        MainActivity.checkIntro(this);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title of toolbar and add back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.tiles);

        addItemsToListOfSettingsFragments();

        setupUI();

        // Add all installed apps to array
        new Thread(() -> {
            if (appsList.size() == 0) {
                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                for (ResolveInfo app : getPackageManager().queryIntentActivities(mainIntent, 0)) {
                    appsList.add(new AppItem(app, activity));
                }
                Collections.sort(appsList);
            }
        }).start();

        // Show tutorial on first launch
        SharedPreferences sp = getSharedPreferences("sp", MODE_PRIVATE);
        if (sp.getBoolean("firstTilesLaunch", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.how_to_use);
            builder.setMessage(R.string.how_to_use_desc);
            builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());
            builder.create().show();
            sp.edit().putBoolean("firstTilesLaunch", false).apply();
        }
    }

    /**
     * Create GUI
     */
    private void setupUI() {
        // On root view click set touchX and touchY to coordinates
        MyRelativeLayout rootTilesLayout = findViewById(R.id.rootTilesLayout);
        assert rootTilesLayout != null;
        rootTilesLayout.setOnInterceptTouchListener((v, ev) -> {
            touchX = (int) ev.getX();
            touchY = (int) ev.getY();
        });

        // Container to add cards to
        LinearLayout cardViewContainer = findViewById(R.id.cardViewContainer);
        assert cardViewContainer != null;
        cardViewContainer.removeAllViews();

        // Declare all the variables used in the for-loop
        TextView title;
        DragSortListView tiles;
        SerializableDashboardTileAdapter tilesAdapter;
        CardView cardView;
        LinearLayout linearLayout;
        for (final SerializableDashboardCategory category : categories) {
            // Create a card for every category
            cardView = new CardView(activity);
            LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardViewParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
            cardView.setLayoutParams(cardViewParams);
            cardView.setRadius(Utils.dpToPx(4, activity));

            // Create LinearLayout as child of card
            linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(
                    getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                    getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin),
                    getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                    getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin)
            );

            // Create TextView for title of category
            title = new TextView(activity);
            title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            title.setTypeface(null, Typeface.BOLD);
            title.setText(category.getTitle());
            linearLayout.addView(title);

            // Create dslv for sorting items in category
            tiles = (DragSortListView) LayoutInflater.from(this).inflate(R.layout.drag_sort_listview, null);
            tiles.setDragEnabled(true);
            tiles.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tilesAdapter = new SerializableDashboardTileAdapter(activity, category.tiles);
            tiles.setAdapter(tilesAdapter);
            tiles.setDropListener(new MyDropListener(categories.indexOf(category), tilesAdapter) {
                @Override
                public void drop(int from, int to) {
                    if (!checkModifications()) {
                        // Move tile to right location on drop
                        List<SerializableDashboardTile> tiles = categories.get(this.number).tiles;
                        SerializableDashboardTile movedItem = tiles.get(from);
                        tiles.remove(from);
                        if (from > to) --from;
                        if (!movedItem.modified) {
                            PreferencesManager pm = PreferencesManager.getInstance();
                            pm.putAndApply(PreferenceConstants.KEY_MODIFICATIONS,
                                    pm.getInteger(PreferenceConstants.KEY_MODIFICATIONS, 0) + 1);
                            movedItem.modified = true;
                        }
                        tiles.add(to, movedItem);
                        saveTiles();
                        setupUI();
                    }
                }
            });
            tiles.setOnItemLongClickListener(new MyItemLongClickListener(categories.indexOf(category), tilesAdapter) {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                    // Remove tile on long click
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.remove_tile);
                    final int index = i;
                    final MyItemLongClickListener listener = this;
                    builder.setPositiveButton(android.R.string.ok, (dialogInterface, i1) -> {
                        if (categories.get(listener.number).tiles.size() > 1) {
                            List<SerializableDashboardTile> tiles1 = categories.get(listener.number).tiles;
                            tiles1.remove(index);
                        } else if (Utils.belowOreo()) categories.remove(listener.number);
                        saveTiles();
                        setupUI();
                    });
                    builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i12) -> {
                    });
                    builder.create().show();
                    return true;
                }
            });
            tiles.setOnItemClickListener(new MyItemClickListener(categories.indexOf(category), tilesAdapter) {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    // Edit tile on single click
                    tilesItemX = touchX;
                    tilesItemY = touchY;

                    SerializableDashboardTile originalTile = categories.get(this.number).tiles.get(i);
                    if (!originalTile.modified && checkModifications()) return;

                    View addTileDialog = addTileDialog(false, this.number, i, tilesItemX, tilesItemY, 0);

                    ((TextView) addTileDialog.findViewById(R.id.title)).setText(originalTile.title);
                    if (categories.get(this.number) != null)
                        ((TextView) addTileDialog.findViewById(R.id.category)).setText(categories.get(this.number).getTitle());
                    if (Utils.notEmpty(originalTile.summary))
                        ((TextView) addTileDialog.findViewById(R.id.description)).setText(originalTile.summary);
                    if (originalTile.intent != null) {
                        if (Utils.belowNougat() || originalTile.intent.mPackage != null) {
                            ((TextView) addTileDialog.findViewById(R.id.packageToLaunch)).setText(originalTile.intent.mPackage);
                        } else {
                            ((TextView) addTileDialog.findViewById(R.id.packageToLaunch)).setText(originalTile.intent.mComponentClass);
                        }
                    }
                    if (originalTile.fragment != null)
                        ((TextView) addTileDialog.findViewById(R.id.fragmentToLaunch)).setText(originalTile.fragment.replace("com.android.settings.", ""));
                    if (Utils.notEmpty(originalTile.iconPath)) {
                        ((ImageView) addTileDialog.findViewById(R.id.iconImage)).setImageDrawable(Drawable.createFromPath(originalTile.iconPath));
                        ((TextView) addTileDialog.findViewById(R.id.iconImageText)).setText("");
                    } else if (originalTile.iconRes != 0x0) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                                ((ImageView) addTileDialog.findViewById(R.id.iconImage)).setImageDrawable(getPackageManager().getResourcesForApplication("com.android.settings").getDrawable(originalTile.iconRes, null));
                            else
                                ((ImageView) addTileDialog.findViewById(R.id.iconImage)).setImageDrawable(getPackageManager().getResourcesForApplication("com.android.settings").getDrawable(originalTile.iconRes));
                            ((TextView) addTileDialog.findViewById(R.id.iconImageText)).setText("");
                        } catch (AndroidException e) {
                            e.printStackTrace();
                        }
                    } else if (originalTile.icon != null && Utils.aboveNougat()) {
                        ImageView icon = addTileDialog.findViewById(R.id.iconImage);
                        icon.setBackgroundColor(Color.WHITE);
                        icon.setImageIcon(originalTile.icon.toIcon());
                        ((TextView) addTileDialog.findViewById(R.id.iconImageText)).setText("");
                    }
                }
            });
            setListViewHeight(tiles);
            linearLayout.addView(tiles);
            cardView.addView(linearLayout);
            cardViewContainer.addView(cardView);
        }

        // Modify FAB
        final FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        assert fabAdd != null;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        fabAdd.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        fabAdd.setOnClickListener(v -> {
            // Show add tile dialog on FAB click
            if (checkModifications()) return;

            final ScrollView modifyTile = findViewById(R.id.modifyTileScrollView);
            assert modifyTile != null;

            final Toolbar toolbar = findViewById(R.id.toolbar);
            assert toolbar != null;

            fabPressed = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                final Point fabPos = new Point(
                        ((int) fabAdd.getX()),
                        ((int) fabAdd.getY())
                );

                tilesItemX = (int) (fabAdd.getX() + fabAdd.getMeasuredWidth() / 2);
                tilesItemY = (int) (fabAdd.getY() + fabAdd.getMeasuredHeight() / 2);

                // Animate FAB to center of screen
                fabAdd.animate()
                        .x((modifyTile.getLeft() + modifyTile.getRight()) / 2 - fabAdd.getMeasuredWidth() / 2)
                        .y((modifyTile.getTop() + modifyTile.getBottom()) / 2 - fabAdd.getMeasuredHeight() / 2)
                        .setDuration(500)
                        .withEndAction(() -> {
                            // On finish move FAB back to original position and show add tile dialog
                            fabAdd.setX(fabPos.x);
                            fabAdd.setY(fabPos.y);

                            addTileDialog(
                                    true,
                                    0,
                                    0,
                                    (modifyTile.getLeft() + modifyTile.getRight()) / 2,
                                    (modifyTile.getLeft() + modifyTile.getRight()) / 2 + toolbar.getMeasuredHeight(),
                                    fabAdd.getMeasuredWidth()
                            );
                        });
            } else {
                // If animate is not supported show addTileDialog directly
                addTileDialog(
                        true,
                        0,
                        0,
                        (modifyTile.getLeft() + modifyTile.getRight()) / 2,
                        (modifyTile.getLeft() + modifyTile.getRight()) / 2 + toolbar.getMeasuredHeight(),
                        0
                );
            }
        });
    }

    /**
     * Show dialog to add or modify a tile
     *
     * @param newTile true if new tile, false if editing existing tile
     * @param cat     category index if editing existing tile
     * @param tile    tile index if editing existing tile
     * @param x       x location of circular reveal
     * @param y       y location of circular reveal
     * @param size    starting size of circular reveal
     * @return view of dialog
     */
    private View addTileDialog(final boolean newTile, final int cat, final int tile, int x, int y, int size) {
        final FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        assert fabAdd != null;
        fabAdd.setVisibility(View.INVISIBLE);

        // Base view for dimensions of animation
        final ScrollView modifyTileScrollView = findViewById(R.id.modifyTileScrollView);
        assert modifyTileScrollView != null;
        modifyTileScrollView.setVisibility(View.VISIBLE);

        // View container to add all views to
        final LinearLayout view = findViewById(R.id.modifyTileContainer);
        assert view != null;
        view.removeAllViews();
        view.addView(getLayoutInflater().inflate(R.layout.dialog_modify_tile, null));
        if (Utils.aboveNougat()) {
            view.findViewById(R.id.textView4).setVisibility(View.GONE);
            view.findViewById(R.id.textView5).setVisibility(View.GONE);
            view.findViewById(R.id.fragmentToLaunch).setVisibility(View.GONE);
        }

        if (Utils.aboveLollipop()) {
            // Create circular reveal if supported
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    modifyTileScrollView,
                    x,
                    y,
                    size,
                    Math.max(modifyTileScrollView.getWidth(), modifyTileScrollView.getHeight()));
            anim.setDuration(500);
            anim.setInterpolator(new AccelerateInterpolator());
            anim.start();
        }

        iconPath = "";
        Button ok = findViewById(R.id.ok);
        assert ok != null;
        ok.setOnClickListener(v -> {
            // Save changes / add new tile
            if (newTile) {
                if (addTile(((TextView) view.findViewById(R.id.title)).getText().toString(),
                        ((TextView) view.findViewById(R.id.packageToLaunch)).getText().toString(),
                        ((TextView) view.findViewById(R.id.fragmentToLaunch)).getText().toString(),
                        iconPath,
                        ((TextView) view.findViewById(R.id.category)).getText().toString(),
                        ((TextView) view.findViewById(R.id.description)).getText().toString())
                        ) {
                    modifyTileScrollView.setVisibility(View.INVISIBLE);
                    fabAdd.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(activity, R.string.dialog_error, Toast.LENGTH_LONG).show();
                    getTiles();
                    setupUI();
                }
            } else if (editTile(cat, tile, ((TextView) view.findViewById(R.id.title)).getText().toString(),
                    ((TextView) view.findViewById(R.id.packageToLaunch)).getText().toString(),
                    ((TextView) view.findViewById(R.id.fragmentToLaunch)).getText().toString(),
                    iconPath,
                    ((TextView) view.findViewById(R.id.category)).getText().toString(),
                    ((TextView) view.findViewById(R.id.description)).getText().toString())
                    ) {
                modifyTileScrollView.setVisibility(View.INVISIBLE);
                fabAdd.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(activity, R.string.dialog_error, Toast.LENGTH_LONG).show();
                getTiles();
                setupUI();
            }
        });

        // Autocomplete for category names
        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.category);
        List<String> categoryNames = new ArrayList<>();
        for (SerializableDashboardCategory category : categories) {
            categoryNames.add(category.getTitle());
        }
        autoCompleteTextView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, categoryNames));

        // Autocomplete for fragments
        AutoCompleteTextView autoCompleteFragmentTextView = view.findViewById(R.id.fragmentToLaunch);
        autoCompleteFragmentTextView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, listOfSettingsFragments));

        iconImageView = view.findViewById(R.id.iconImage);
        iconImageView.setOnClickListener(this::showImageDialog);
        iconTextView = view.findViewById(R.id.iconImageText);

        final TextView packageTextView = view.findViewById(R.id.packageToLaunch);
        Button packageButton = view.findViewById(R.id.packageToLaunchButton);
        packageButton.setOnClickListener(view12 -> {
            // On Apps button click, show list of installed apps
            if (appsList.size() == 0) {
                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                for (ResolveInfo app : getPackageManager().queryIntentActivities(mainIntent, 0)) {
                    appsList.add(new AppItem(app, activity));
                }
                Collections.sort(appsList);
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            ListView lv = new ListView(activity);
            builder.setView(lv);
            final AlertDialog appsListDialog = builder.create();
            appsListDialog.show();

            lv.setAdapter(new AppItemAdapter(activity, appsList));
            lv.setOnItemClickListener((adapterView, view1, i, l) -> {
                packageTextView.setText(appsList.get(i).pkg);
                appsListDialog.dismiss();
            });
        });
        return view;
    }

    /**
     * Edit an existing tile
     *
     * @param category     index of category
     * @param tile         index of tile
     * @param title        title of the tile
     * @param intentPkg    package of the tile
     * @param fragment     fragment of the tile
     * @param iconPath     path to icon for the tile
     * @param categoryName category name for the tile
     * @return success
     */
    private boolean editTile(int category, int tile, String title, String intentPkg, String fragment, String iconPath, String categoryName, String description) {
        // Return if both fragment and package are entered or category or title is empty
        if (!fragment.equals("") && !intentPkg.equals("")) return false;
        if (categoryName.equals("") || title.equals("")) return false;

        // Get the original tile
        SerializableDashboardCategory originalCategory = categories.get(category);
        SerializableDashboardTile originalTile = originalCategory.tiles.get(tile);

        // Modify original tile
        originalTile.title = title;
        originalTile.summary = description;
        if (Utils.notEmpty(intentPkg) && !(originalTile.intent != null && ((originalTile.intent.mPackage != null && originalTile.intent.mPackage.equals(intentPkg)) || (originalTile.intent.mComponentClass != null && originalTile.intent.mComponentClass.equals(intentPkg))))) {
            if (intentPkg.toLowerCase().equals(intentPkg)) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(intentPkg);
                originalTile.intent = new SerializableIntent(launchIntent);
            } else {
                Intent intent = new Intent();
                String[] parts = intentPkg.split("\\.");
                String pkg = "";
                for (String part : parts) {
                    if (part.toLowerCase().equals(part)) {
                        pkg += part + ".";
                    } else {
                        break;
                    }
                }
                pkg = pkg.substring(0, pkg.length() - 1);
                intent.setComponent(new ComponentName(pkg, intentPkg));
                if (intentPkg.startsWith("com.android.settings")) {
                    intent.putExtra("com.android.settings.FRAGMENT_CLASS", intentPkg);
                }
                originalTile.intent = new SerializableIntent(intent);
            }
            originalTile.fragment = null;
            originalTile.fragmentArguments = null;
        } else if (Utils.notEmpty(fragment) && !(originalTile.fragment != null && originalTile.fragment.equals(fragment))) {
            originalTile.intent = null;
            if (fragment.startsWith("com.")) originalTile.fragment = fragment;
            else originalTile.fragment = "com.android.settings." + fragment;
            originalTile.fragmentArguments = null;
        }
        if (Utils.notEmpty(iconPath)) {
            originalTile.iconPath = iconPath;
            originalTile.iconRes = 0x0;
            originalTile.iconPkg = null;
        }
        if (!originalTile.modified) {
            PreferencesManager pm = PreferencesManager.getInstance();
            pm.putAndApply(PreferenceConstants.KEY_MODIFICATIONS,
                    pm.getInteger(PreferenceConstants.KEY_MODIFICATIONS, 0) + 1);
            originalTile.modified = true;
        }
        if (!categoryName.equals(originalCategory.getTitle())) {
            // If category changed,
            // check if category exists and if so, add tile to category
            boolean added = false;
            for (SerializableDashboardCategory cat : categories) {
                if (cat.getTitle().equals(categoryName)) {
                    cat.tiles.add(originalTile);
                    if (Utils.aboveOreo()) {
                        originalTile.category = null;
                        if (originalTile.metaData != null && originalTile.metaData.map.containsKey("com.android.settings.category")) {
                            originalTile.metaData.map.remove("com.android.settings.category");
                        }
                    }
                    added = true;
                }
            }

            // If category doesn't exist, create a new category
            if (!added && Utils.belowOreo()) {
                SerializableDashboardCategory cat = new SerializableDashboardCategory();
                cat.title = categoryName;
                cat.tiles.add(originalTile);
                categories.add(cat);
            }
            if (originalCategory.tiles.size() > 1) originalCategory.tiles.remove(tile);
            else if (Utils.belowOreo()) categories.remove(category);
        }

        // Save changes to storage and refresh UI
        saveTiles();
        setupUI();
        return true;
    }

    /**
     * Add a new tile to a category
     *
     * @param title     title of the new tile
     * @param intentPkg package of the new tile
     * @param fragment  fragment of the new tile
     * @param iconPath  path to icon for the new tile
     * @param category  category name for the new tile
     * @return success
     */
    private boolean addTile(String title, String intentPkg, String fragment, String iconPath, String category, String description) {
        // Return if both fragment and package are entered or category or title is empty
        if (!fragment.equals("") && !intentPkg.equals("")) return false;
        if (category.equals("") || title.equals("")) return false;

        // Create new tile and set properties
        SerializableDashboardTile tile = new SerializableDashboardTile();
        tile.title = title;
        if (!intentPkg.equals(""))
            tile.intent = new SerializableIntent(getPackageManager().getLaunchIntentForPackage(intentPkg));
        if (!fragment.equals("")) {
            if (fragment.startsWith("com.")) tile.fragment = fragment;
            else tile.fragment = "com.android.settings." + fragment;
        }
        tile.iconPath = iconPath;
        tile.summary = description;
        if (!tile.modified) {
            PreferencesManager pm = PreferencesManager.getInstance();
            pm.putAndApply(PreferenceConstants.KEY_MODIFICATIONS,
                    pm.getInteger(PreferenceConstants.KEY_MODIFICATIONS, 0) + 1);
            tile.modified = true;
        }

        // Check if category exists and if so, add tile to category
        boolean added = false;
        for (SerializableDashboardCategory cat : categories) {
            if (cat.getTitle().equals(category)) {
                cat.tiles.add(tile);
                added = true;
            }
        }
        // If category doesn't exist, create a new category
        if (!added && Utils.belowOreo()) {
            SerializableDashboardCategory cat = new SerializableDashboardCategory();
            cat.title = category;
            cat.tiles.add(tile);
            categories.add(cat);
        }

        // Save changes to storage and refresh UI
        saveTiles();
        setupUI();
        Toast.makeText(this, R.string.setting_added, Toast.LENGTH_LONG).show();
        return true;
    }

    /**
     * Options menu inflater
     *
     * @param menu Menu to be inflated
     * @return success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tiles, menu);
        return true;
    }

    /**
     * On option in ActionBar selected
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
                    setupUI();
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
     * On activity result save icon from image to storage
     *
     * @param requestCode request code set by the app
     * @param resultCode  result code
     * @param data        data received by the app
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                int iconSize = am.getLauncherLargeIconSize();
                Bitmap image = Bitmap.createScaledBitmap(Utils.getBitmapFromUri(uri, activity), iconSize, iconSize, false);
                int index = 1;
                File destination = new File(IOManager.FILEPATH, "image" + index + ".png");
                while (destination.exists()) {
                    index++;
                    destination = new File(IOManager.FILEPATH, "image" + index + ".png");
                }
                if (destination.createNewFile()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 0, bos);
                    byte[] bitmapdata = bos.toByteArray();

                    FileOutputStream fos = new FileOutputStream(destination);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    if (destination.canRead() || destination.setReadable(true, true)) {
                        iconPath = destination.getPath();
                        assert iconImageView != null;
                        iconImageView.setImageDrawable(Drawable.createFromPath(iconPath));
                        iconTextView.setText("");
                    } else {
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException | NullPointerException e) {
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Set ListView to full height
     *
     * @param listView listView to set height of
     */
    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * Choose icon from local storage or from app icon
     *
     * @param v Clicked view
     **/
    private void showImageDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        ListView listView = new ListView(this);
        String[] data = new String[]{
                getResources().getString(R.string.icon_by_app),
                getResources().getString(R.string.icon_by_list),
                getResources().getString(R.string.icon_by_fm)
        };
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
        builder.setView(listView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    if (appsList.size() == 0) {
                        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                        for (ResolveInfo app : getPackageManager().queryIntentActivities(mainIntent, 0)) {
                            appsList.add(new AppItem(app, activity));
                        }
                        Collections.sort(appsList);
                    }

                    final AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    ListView lv = new ListView(activity);
                    builder1.setView(lv);
                    final AlertDialog appsListDialog = builder1.create();
                    appsListDialog.show();

                    lv.setAdapter(new AppItemAdapter(activity, appsList));
                    lv.setOnItemClickListener((adapterView, view12, i, l) -> {
                        Drawable icon = appsList.get(i).drawable;

                        int index = 1;
                        File destination = new File(IOManager.FILEPATH, "image" + index + ".png");
                        while (destination.exists()) {
                            index++;
                            destination = new File(IOManager.FILEPATH, "image" + index + ".png");
                        }
                        IOManager.writeDrawable(icon, destination.getName());
                        iconPath = destination.getPath();
                        iconImageView.setImageDrawable(Drawable.createFromPath(iconPath));
                        iconTextView.setText("");
                        appsListDialog.dismiss();
                    });
                    break;
                case 1:
                    try {
                        List<String> fileNames = new ArrayList<>();
                        for (String fileName : getAssets().list("")) {
                            if (fileName.endsWith("png")) fileNames.add(fileName);
                        }

                        GalleryView galleryView = new GalleryView(TilesActivity.this);

                        AlertDialog.Builder iconListBuilder = new AlertDialog.Builder(TilesActivity.this);
                        iconListBuilder.setCancelable(true);
                        iconListBuilder.setView(galleryView);
                        final AlertDialog iconListDialog = iconListBuilder.create();
                        iconListDialog.show();
                        galleryView.setImagesFromAssets(fileNames);

                        galleryView.setOnItemClickListener((parent1, view1, position1, id1) -> {
                            int index = 1;
                            File destination = new File(IOManager.FILEPATH, "image" + index + ".png");
                            while (destination.exists()) {
                                index++;
                                destination = new File(IOManager.FILEPATH, "image" + index + ".png");
                            }
                            IOManager.writeDrawable(((ImageView) view1).getDrawable(), destination.getName());
                            iconPath = destination.getPath();
                            iconImageView.setImageDrawable(Drawable.createFromPath(iconPath));
                            iconTextView.setText("");
                            iconListDialog.dismiss();
                        });
                    } catch (IOException e) {
                        Toast.makeText(TilesActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    Utils.showFileChooser(activity);
            }
            alertDialog.dismiss();
        });
    }

    /**
     * Fill listOfSettingsFragments with array of settings fragments found from com.android.settings' AndroidManifest.xml
     */
    private void addItemsToListOfSettingsFragments() {
        PackageManager pManager = activity.getPackageManager();
        try {
            PackageInfo list = pManager.getPackageInfo("com.android.settings", PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            for (ActivityInfo activity : list.activities) {
                if (activity.metaData != null) {
                    String fragmentClass = (String) activity.metaData.get("com.android.settings.FRAGMENT_CLASS");
                    if (fragmentClass != null)
                        listOfSettingsFragments.add(fragmentClass.replace("com.android.settings.", ""));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        listOfSettingsFragments = Utils.removeDuplicates(listOfSettingsFragments);
        Collections.sort(listOfSettingsFragments);
    }

    /**
     * Called when device back button is pressed or ActionBar back button is pressed
     */
    @Override
    public void onBackPressed() {
        final View modifyTile = findViewById(R.id.modifyTileScrollView);
        assert modifyTile != null;

        // If add tile dialog is visible hide it and discard changes
        if (modifyTile.getVisibility() == View.VISIBLE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.discard);
            builder.setNegativeButton(android.R.string.no, null);
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                final View fab = findViewById(R.id.fabAdd);
                assert fab != null;

                if (Utils.aboveLollipop()) {
                    // Show reversed circular reveal if supported
                    int endSize = 0;
                    if (fabPressed) {
                        endSize = fab.getMeasuredHeight();
                        fabPressed = false;
                    }
                    Animator anim = ViewAnimationUtils.createCircularReveal(
                            modifyTile,
                            tilesItemX,
                            tilesItemY,
                            Math.max(modifyTile.getWidth(), modifyTile.getHeight()),
                            endSize);
                    anim.setDuration(500);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.start();
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            modifyTile.setVisibility(View.INVISIBLE);
                            fab.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                } else {
                    modifyTile.setVisibility(View.INVISIBLE);
                    fab.setVisibility(View.VISIBLE);
                }
            });
            builder.show();
        }
        // Else close activity
        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.hold, R.anim.right_slide_out);
        }
    }
}