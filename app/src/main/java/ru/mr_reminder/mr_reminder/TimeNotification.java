package ru.mr_reminder.mr_reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public class TimeNotification extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        Map<String, String> memento = new HashMap<>();

        if (extras != null) {
            memento = (Map<String, String>) extras.getSerializable("memento");
        }


        assert memento != null;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(memento.get("name"))
                        .setContentText(memento.get("text"));

//        NotificationCompat.Builder builder =
//                new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setContentTitle(memento.get("name"))
//                .setContentText(memento.get("text"))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

//Интент для активити, которую мы хотим запускать при нажатии на уведомление
        Intent intentTL = new Intent(context, AddMementoActivity.class);
        intent.putExtra("id", memento.get("id"));

        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        notification.sound = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Ringtones/Little_Big_-_Skibidi_(ringon.pro).mp3");
        assert notificationManager != null;

        System.out.println(builder);

        notificationManager.notify(Integer.parseInt(memento.get("id")), notification);

    }
}