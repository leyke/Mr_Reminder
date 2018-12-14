package ru.mr_reminder.mr_reminder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

//import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    ListView mementoList;
    DBHelper databaseHelper;
    SQLiteDatabase db;
    Cursor mementoCursor;
    SimpleCursorAdapter mementoAdapter;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mementoList = (ListView)findViewById(R.id.list);
        mementoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), AddMementoActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        databaseHelper = new DBHelper(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.getReadableDatabase();

        mementoCursor =  db.rawQuery("select * from "+ AddMementoActivity.TABLE_NAME, null);
        String[] headers = new String[] {AddMementoActivity.Cols.NAME, AddMementoActivity.Cols.DATETIME};
        mementoAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
                mementoCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        mementoList.setAdapter(mementoAdapter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
        mementoCursor.close();
    }
    // Метод обработки нажатия на кнопку
    public void createMemento(View view) {
        Intent intent = new Intent(this, AddMementoActivity.class);
        startActivity(intent);
    }
}
