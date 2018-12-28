package ru.mr_reminder.mr_reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

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


        mementoList = (ListView) findViewById(R.id.list);
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

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.getReadableDatabase();

        mementoCursor = db.rawQuery("select * from " + AddMementoActivity.TABLE_NAME, null);
        if (mementoCursor.moveToFirst()) {
            do {
                System.out.println(mementoCursor.getString(mementoCursor.getColumnIndex(AddMementoActivity.Cols.NAME)));
                restartNotify(mementoCursor);

            } while (mementoCursor.moveToNext());
        }
        mementoCursor.moveToFirst();
        String[] headers = new String[]{AddMementoActivity.Cols.NAME, AddMementoActivity.Cols.DATETIME};
        mementoAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
                mementoCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        mementoList.setAdapter(mementoAdapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
        mementoCursor.close();
    }

    public void createMemento(View view) {
        Intent intent = new Intent(this, AddMementoActivity.class);
        startActivity(intent);
    }

    private void restartNotify(Cursor MementoCursor) {
        String dtStr = MementoCursor.getString(MementoCursor.getColumnIndex(AddMementoActivity.Cols.DATETIME));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        Date date = Calendar.getInstance().getTime();
        Map<String, String> memento = new HashMap<>();
        memento.put("id", MementoCursor.getString(0));
        memento.put("name", MementoCursor.getString(MementoCursor.getColumnIndex(AddMementoActivity.Cols.NAME)));
        memento.put("text", MementoCursor.getString(MementoCursor.getColumnIndex(AddMementoActivity.Cols.DESCRIPTION)));

        try {
            date = format.parse(dtStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long time = date.getTime();
        Date resultdate = new Date(System.currentTimeMillis());
        System.out.println(format.format(resultdate));
        System.out.println(date);
        if (time > System.currentTimeMillis()) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, TimeNotification.class);
            intent.putExtra("memento", (Serializable) memento);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                    intent, FLAG_UPDATE_CURRENT);


            assert am != null;
            am.set(AlarmManager.RTC, time, pendingIntent);
        }
    }
}
