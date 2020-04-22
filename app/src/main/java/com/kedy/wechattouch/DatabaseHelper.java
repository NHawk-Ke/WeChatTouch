package com.kedy.wechattouch;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME
                + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " TITLE TEXT NOT NULL,"
                + " ICON INTEGER,"
                + " DATE TEXT NOT NULL,"
                + " TIME TEXT NOT NULL,"
                + " DESCRIPTION TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insert(String title, Integer icon, String datetime, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, title);
        contentValues.put(COL_3, icon);
        String[] datetimeStr = datetime.split(" ");
        contentValues.put(COL_4, datetimeStr[0]);
        contentValues.put(COL_5, datetimeStr[1]);
        contentValues.put(COL_6, description);
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }
}
