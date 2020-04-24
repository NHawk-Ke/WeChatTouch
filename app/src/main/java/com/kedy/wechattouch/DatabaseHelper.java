package com.kedy.wechattouch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "Kedy.db";
    private static final String TABLE_NAME = "calendar_table";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "TITLE";
    private static final String COL_3 = "ICON";
    private static final String COL_4 = "DATE";
    private static final String COL_5 = "TIME";
    private static final String COL_6 = "DESCRIPTION";
    private static final String COL_7 = "STATUS";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS calendar_table " +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT NOT NULL, ICON INTEGER, " +
                "DATE TEXT NOT NULL, TIME TEXT NOT NULL, DESCRIPTION TEXT, STATUS INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    boolean insert(String title, Integer icon, String datetime, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, title);
        contentValues.put(COL_3, icon);
        String[] datetimeStr = datetime.split(" ");
        contentValues.put(COL_4, datetimeStr[0]);
        contentValues.put(COL_5, datetimeStr[1]);
        contentValues.put(COL_6, description);
        contentValues.put(COL_7, 0);
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }

    public int[] getIcons(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COL_3;
        query += " FROM " + TABLE_NAME;
        query += " WHERE " + COL_4 + "='" + date;
        query += "' AND " + COL_3 + "<>" + R.drawable.ic_empty;
        query += " AND " + COL_7 + "=0";
        query += " ORDER BY " + COL_5;
        query += " LIMIT 3";

        Cursor cursor = db.rawQuery(query, null);
        int[] result = new int[cursor.getCount()];

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToNext();
            result[i] = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    Map<String, List> getDayPlans(String date, String status) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * ";
        query += " FROM " + TABLE_NAME;
        query += " WHERE " + COL_4 + "='" + date + "'";
        query += " AND " + COL_7 + "=" + status;
        query += " ORDER BY " + COL_5;

        Cursor cursor = db.rawQuery(query, null);

        List<Integer> IDList = new ArrayList<>();
        List<String> TitleList = new ArrayList<>();
        List<Integer> IconList = new ArrayList<>();
        List<String> TimeList = new ArrayList<>();
        List<String> DescriptionList = new ArrayList<>();
        List<Integer> StatusList = new ArrayList<>();

        while (cursor.moveToNext()){
            IDList.add(cursor.getInt(0));
            TitleList.add(cursor.getString(1));
            IconList.add(cursor.getInt(2));
            TimeList.add(cursor.getString(4));
            DescriptionList.add(cursor.getString(5));
            StatusList.add(cursor.getInt(6));
        }
        cursor.close();

        Map<String, List> result = new HashMap<>();

        result.put("ID", IDList);
        result.put("Titles", TitleList);
        result.put("Icons", IconList);
        result.put("Times", TimeList);
        result.put("Descriptions", DescriptionList);
        result.put("Status", StatusList);

        return result;
    }

    public int[] getPlanStatus(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(CASE WHEN " + COL_7 + "=1 THEN 1 END), COUNT(*) ";
        query += " FROM " + TABLE_NAME;
        query += " WHERE " + COL_4 + "='" + date + "'";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        int[] result = new int[]{cursor.getInt(0), cursor.getInt(1)};
        cursor.close();

        return result;
    }

    void setPlanStatus(Integer planId, Integer status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COL_7, status);
        db.update(TABLE_NAME, cv, COL_1 + "=" + planId, null);
    }

    void deletePlan(Integer planId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME,COL_1 + "=" + planId, null);
    }

    private void checkTable() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            Log.d(TAG, "数据：" + cursor.getInt(0) + " " + cursor.getString(1)
                    + " " + cursor.getInt(2) + " " + cursor.getString(3) +
                    " " + cursor.getString(4) + " " + cursor.getString(5) +
                    " " + cursor.getInt(6));
        }
        cursor.close();
    }
}
