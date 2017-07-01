package com.example.interactive.backupandrestoredb;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Button createBtn, readBtn, updateBtn, deleteBtn, backupBtn, restoreBtn;
    private SQLiteDatabase db;
    private DatabaseHelper helper;
    EditText tid, name, score, newScore, sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createBtn = (Button) findViewById(R.id.button);
        readBtn = (Button) findViewById(R.id.button2);
        updateBtn = (Button) findViewById(R.id.button3);
        deleteBtn = (Button) findViewById(R.id.button4);
        backupBtn = (Button) findViewById(R.id.button5);
        restoreBtn = (Button) findViewById(R.id.button6);

        tid = (EditText) findViewById(R.id.editText);
        name = (EditText) findViewById(R.id.editText2);
        score = (EditText) findViewById(R.id.editText3);

        helper = new DatabaseHelper(this);
        db = helper.getWritableDatabase();

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id;
                ContentValues cv = new ContentValues();
                cv.put("_id", Integer.parseInt(tid.getText().toString()));
                cv.put("name", name.getText().toString());
                cv.put("score", Double.parseDouble(score.getText().toString()));
                id = db.insert("tbl_todos", null, cv);
                Log.i(TAG, "Inserted: " + id);
            }
        });

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlQuery("SELECT * FROM " + "tbl_todos");
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count;
                int id = Integer.parseInt(tid.getText().toString());
                ContentValues cv = new ContentValues();
                cv.put("score", Double.parseDouble(score.getText().toString()));
                count = db.update("tbl_todos", cv, "_id=" + id, null);
                Log.i(TAG, "Updated: " + count);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count;
                int id = Integer.parseInt(tid.getText().toString());
                count = db.delete("tbl_todos" + "", "_id=" + id, null);
                Log.i(TAG, "Deleted: " + count);
            }
        });

        backupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backup();
            }
        });

        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
            }
        });

        tables();
    }

    public void restore() {
        try {
            File cacheDir = getApplication().getCacheDir();
            File data = Environment.getDataDirectory();

            if (cacheDir.canWrite()) {
                String currentDBPath = "/data/" + getApplicationContext().getPackageName() + "/databases/" + DatabaseHelper.DATABASE_NAME;
                String backupDBPath = DatabaseHelper.DATABASE_BACKUP_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(cacheDir, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(), "Database Restored successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }

    public void backup() {
        try {
            File cacheDir = getApplication().getCacheDir();
            File data = Environment.getDataDirectory();

            if (cacheDir.canWrite()) {
                String currentDBPath = "/data/" + getApplicationContext().getPackageName() + "/databases/" + DatabaseHelper.DATABASE_NAME;
                String backupDBPath = DatabaseHelper.DATABASE_BACKUP_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(cacheDir, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(), "Backup is successful to SD card", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sqlQuery(String sql) {
        String[] colNames;
        String str = "";
        Cursor c = db.rawQuery(sql, null);
        colNames = c.getColumnNames();
        for (int i = 0; i < colNames.length; i++) {
            str += colNames[i] + "\t\t";
        }
        str += "\n";
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            str += c.getString(0) + "\t";
            str += c.getString(1) + "\t";
            str += c.getString(2) + "\n";
            c.moveToNext();
        }
        Log.i(TAG, str.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_freq_activity:
                startActivity(new Intent(this, FreqActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void tables() {
        Cursor cursor1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        String table;
        Map<String, List<Map>> data = new HashMap<>();
        List<Map> datatable;
        try {
            if (cursor1.moveToNext()) {
                do {
                    table = cursor1.getString(0);
                    if (table.startsWith("tbl_")) {
                        datatable = new ArrayList<>();
                        data.put(table, datatable);
                        Cursor cursor2 = db.rawQuery("SELECT * FROM " + table, null);
                        try {
                            String[] columnNames = cursor2.getColumnNames();
                            if (cursor2.moveToNext()) {
                                do {
                                    Map map = new HashMap();
                                    datatable.add(map);
                                    int index, type;
                                    for (String columnName : columnNames) {
                                        index = cursor2.getColumnIndex(columnName);
                                        type = cursor2.getType(index);
                                        switch (type) {
                                            case Cursor.FIELD_TYPE_INTEGER:
                                                map.put(columnName, cursor2.getInt(index));
                                                break;
                                            case Cursor.FIELD_TYPE_FLOAT:
                                                map.put(columnName, cursor2.getFloat(index));
                                                break;
                                            case Cursor.FIELD_TYPE_BLOB:
                                                map.put(columnName, cursor2.getBlob(index));
                                                break;
                                            default:
                                                map.put(columnName, cursor2.getString(index));
                                                break;
                                        }
                                    }
                                } while (cursor2.moveToNext());
                            }
                        } finally {
                            cursor2.close();
                        }
                    }
                }
                while (cursor1.moveToNext());
            }
        } finally {
            if (cursor1 != null) {
                cursor1.close();
            }
        }
        String message = new Gson().toJson(data);
        Log.i(TAG, message);
    }
}
