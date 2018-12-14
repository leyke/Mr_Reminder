package ru.mr_reminder.mr_reminder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddMementoActivity extends AppCompatActivity {
    public static final String TABLE_NAME = "Memento";

    public static final class Cols {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String DATETIME = "datetime";
        public static final String PHOTO = "photo";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";

    }
    EditText nameBox;
    EditText descriptionBox;
    EditText datetimeBox;
    Button delButton;
    Button saveButton;

    DBHelper sqlHelper;
    SQLiteDatabase db;
    Cursor mementoCursor;
    long mementoId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memento);

        nameBox = (EditText) findViewById(R.id.name);
        descriptionBox = (EditText) findViewById(R.id.description);
        datetimeBox = (EditText) findViewById(R.id.datetime);
        delButton = (Button) findViewById(R.id.deleteButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        sqlHelper = new DBHelper(this);
        db = sqlHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mementoId = extras.getLong("id");
        }
        // если 0, то добавление
        if (mementoId > 0) {
            // получаем элемент по id из бд
            mementoCursor = db.rawQuery("select * from " + TABLE_NAME + " where " +
                    Cols.ID + "=?", new String[]{String.valueOf(mementoId)});
            mementoCursor.moveToFirst();
            nameBox.setText(mementoCursor.getString(mementoCursor.getColumnIndex(Cols.NAME)));
            descriptionBox.setText(mementoCursor.getString(mementoCursor.getColumnIndex(Cols.DESCRIPTION)));
            datetimeBox.setText(mementoCursor.getString(mementoCursor.getColumnIndex(Cols.DATETIME)));

            mementoCursor.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }
    }

    public void save(View view){
        ContentValues cv = new ContentValues();
        cv.put(Cols.NAME, nameBox.getText().toString());
        cv.put(Cols.DESCRIPTION, descriptionBox.getText().toString());
        cv.put(Cols.DATETIME, datetimeBox.getText().toString());

        if (mementoId > 0) {
            db.update(TABLE_NAME, cv, Cols.ID + "=" + String.valueOf(mementoId), null);
        } else {
            db.insert(TABLE_NAME, null, cv);
        }
        goHome();
    }
    public void delete(View view){
        db.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(mementoId)});
        goHome();
    }
    private void goHome(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
