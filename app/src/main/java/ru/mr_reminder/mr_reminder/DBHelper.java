package ru.mr_reminder.mr_reminder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 4;
    private static final String DATABASE_NAME = "Mr_reminder.db";

    DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DATABASE_NAME, null, VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE table " + "Memento" + "(" +
                "_id integer primary key autoincrement," +
//                AddMementoActivity.Cols.ID + "," +
                AddMementoActivity.Cols.NAME + "," +
                AddMementoActivity.Cols.DESCRIPTION + "," +
                AddMementoActivity.Cols.DATETIME + "," +
                AddMementoActivity.Cols.PHOTO + "," +
                AddMementoActivity.Cols.LONGITUDE + "," +
                AddMementoActivity.Cols.LATITUDE + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AddMementoActivity.TABLE_NAME);

        onCreate(db);
    }
}