package com.kedy.wechattouch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

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

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS calendar_table " +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT NOT NULL, ICON INTEGER, " +
                "DATE TEXT NOT NULL, TIME TEXT NOT NULL, DESCRIPTION TEXT)");
        Log.d(TAG, "数据库开始");
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
        String[] date = datetimeStr[0].split("-");
        contentValues.put(COL_4, date[0] + "-" + (Integer.parseInt(date[1]) - 1) + "-" + (Integer.parseInt(date[2]) - 1));
        contentValues.put(COL_5, datetimeStr[1]);
        contentValues.put(COL_6, description);
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }

    public int[] getIcons(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COL_3;
        query += " FROM " + TABLE_NAME;
        query += " WHERE " + COL_4 + "='" + date;
        query += "' AND " + COL_3 + "<>" + R.drawable.ic_empty;
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

    void checkTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        Log.d(TAG, "检查");

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToNext();
            int c1 = cursor.getInt(0);
            String c2 = cursor.getString(1);
            int c3 = cursor.getInt(2);
            String c4 = cursor.getString(3);
            String c5 = cursor.getString(4);
            String c6 = cursor.getString(5);

            Log.d(TAG, c1 + " " + c2 + " " + c3 + " " + c4 + " " + c5 + " " + c6);
        }
        cursor.close();
    }
}
