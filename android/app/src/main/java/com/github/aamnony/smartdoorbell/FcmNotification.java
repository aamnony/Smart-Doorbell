package com.github.aamnony.smartdoorbell;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Map;

public class FcmNotification {

    private static final int ID = 0xC2FF8;

    public static void post(Map<String, String> data, Context context) {
        NotificationCompat.Builder builder = buildNotification(data, context);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ID, builder.build());
    }

    private static NotificationCompat.Builder buildNotification(Map<String, String> data, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Unrecognized Person At Your Door");

        Intent streamIntent = new Intent(context, StreamActivity.class);
        streamIntent.putExtra(StreamActivity.EXTRA_USER_NAME, data.get(StreamActivity.EXTRA_USER_NAME));
        builder.addAction(android.R.drawable.presence_video_online, "View Stream", PendingIntent.getActivity(context, ID, streamIntent, 0));


        // Add onClick behaviour.
//        builder.setContentIntent(PendingIntent.getActivity(context, ID, streamIntent, 0));

        return builder;
    }

}
