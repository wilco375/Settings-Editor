package com.wilco375.settingseditor.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wilco375.settingseditor.R;
import com.wilco375.settingseditor.general.IOManager;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BackupActivity extends AppCompatActivity {

    final String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Settings Editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title of toolbar and add back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.backup);

        // Backup
        Button backupButton = findViewById(R.id.backupButton);
        final EditText backupName = findViewById(R.id.backupName);
        String backupNameStr = "Settings Editor Backup - " + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Calendar.getInstance().getTime());
        backupName.setText(backupNameStr);

        backupButton.setOnClickListener(view -> createBackup(backupName.getText().toString()));

        // Restore
        updateList();
    }

    private void updateList(){
        final ListView restoreListView = findViewById(R.id.restoreListView);
        List<String> backupFiles = new ArrayList<>();
        if(new File(backupPath).listFiles() != null) {
            for (File file : new File(backupPath).listFiles()) {
                backupFiles.add(file.getName().replace(".se", ""));
            }
            restoreListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, backupFiles));

            restoreListView.setOnItemClickListener((adapterView, view, i, l) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this);
                builder.setTitle(R.string.restore_confirm);
                builder.setMessage(R.string.restore_confirm_desc);
                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i14) -> { });
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i13) -> restoreFile(((TextView) view.findViewById(android.R.id.text1)).getText().toString()));
                builder.show();
            });
            restoreListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this);
                builder.setTitle(R.string.backup_delete);
                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i12) -> { });
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i1) -> {
                    new File(backupPath, ((TextView) view.findViewById(android.R.id.text1)).getText().toString()+".se").delete();

                    updateList();
                });
                builder.show();

                return true;
            });

            setListViewHeight(restoreListView);
        }
    }

    private void createBackup(String fileName){
        try {
            File zipOut = new File(backupPath, fileName + ".se");
            zipOut.getParentFile().mkdirs();
            zipOut.delete();

            ZipFile zipFile = new ZipFile(zipOut);
            ZipParameters zipParams = new ZipParameters();
            zipParams.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParams.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zipParams.setIncludeRootFolder(false);

            zipFile.addFolder(IOManager.FILEPATH, zipParams);

            Toast.makeText(this, R.string.backup_success, Toast.LENGTH_LONG).show();

            updateList();
        }catch (ZipException e){
            e.printStackTrace();
            Toast.makeText(this, R.string.backup_error, Toast.LENGTH_LONG).show();
        }
    }

    private void restoreFile(String fileName){
        try{
            File zipIn = new File(backupPath, fileName + ".se");
            File dirOut = new File(IOManager.FILEPATH);
            for(File file : dirOut.listFiles()){
                file.delete();
            }

            ZipFile zipFile = new ZipFile(zipIn);
            zipFile.extractAll(dirOut.toString());

            Toast.makeText(this, R.string.restore_success, Toast.LENGTH_LONG).show();
        }catch (ZipException e){
            e.printStackTrace();
            Toast.makeText(this, R.string.restore_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set ListView to full height
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
}
