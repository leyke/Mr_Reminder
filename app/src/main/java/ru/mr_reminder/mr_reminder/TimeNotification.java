package ru.mr_reminder.mr_reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
        Long mementoId = Long.valueOf(memento.get("id"));
        Intent intentTL = new Intent(context, AddMementoActivity.class);

        System.out.println(mementoId);
        intentTL.putExtra("id", mementoId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(memento.get("id")), intentTL, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(memento.get("name"))
                        .setContentText(memento.get("text"))
                        .addAction(R.mipmap.ic_launcher, "Открыть", pendingIntent)
                ;

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        notification.sound = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Ringtones/Little_Big_-_Skibidi_(ringon.pro).mp3");
        assert notificationManager != null;

        System.out.println(builder);

        notificationManager.notify(Integer.parseInt(memento.get("id")), notification);

    }
}