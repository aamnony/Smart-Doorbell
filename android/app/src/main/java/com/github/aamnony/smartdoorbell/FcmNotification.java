package com.github.aamnony.smartdoorbell;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import java.util.Map;

public class FcmNotification {

    public static void post(Map<String, String> data, Context context) {
        int notificationId = ("SmartDoorbell:" + AppHelper.getCurrUser() + "/" + data.get("camera_name")).hashCode();
        NotificationCompat.Builder builder = buildNotification(data, context, createChannel(context), notificationId);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, builder.build());
    }

    private static String createChannel(Context context) {
        String name = context.getString(R.string.app_name);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        return name;
    }

    private static NotificationCompat.Builder buildNotification(Map<String, String> data, Context context, String channel, int notificationId) {
        String snapshotId = data.get("image_name");
        String cameraName = data.get("camera_name");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Unrecognized Person At Your Door @" + cameraName)
                .setDefaults(Notification.DEFAULT_ALL);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                AppHelper.IDENTITIES_POOL_ID,
                AppHelper.IOT_REGION
        );
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);
        s3Client.setRegion(Region.getRegion(AppHelper.COGNITO_REGION));

        S3Object s3Object = s3Client.getObject(AppHelper.S3_BUCKET_NAME, snapshotId);
        Bitmap bmp = BitmapFactory.decodeStream(s3Object.getObjectContent());
        new NotificationCompat.BigPictureStyle().bigPicture(bmp).setBuilder(builder);

        Intent streamIntent = new Intent(context, StreamActivity.class);
        streamIntent.putExtra(StreamActivity.CAMERA_NAME, cameraName);
        streamIntent.putExtra(StreamActivity.NOTIFICATION_ID, notificationId);
        builder.addAction(
                android.R.drawable.ic_menu_camera,
                context.getString(R.string.stream),
                PendingIntent.getActivity(context, 0, streamIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        );

        Intent unlockIntent = new Intent(context, MessagesActivity1.class);
        unlockIntent.putExtra(MessagesActivity.CAMERA_NAME, cameraName);
        unlockIntent.putExtra(MessagesActivity.SNAPSHOT_ID, snapshotId);
        unlockIntent.putExtra(MessagesActivity.ADD, "0");
        unlockIntent.putExtra(MessagesActivity.NOTIFICATION_ID, notificationId);
        builder.addAction(
                android.R.drawable.ic_lock_lock,
                context.getString(R.string.unlock),
                PendingIntent.getActivity(context, 0, unlockIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        );

        Intent unlockAndAddIntent = new Intent(context, MessagesActivity.class);
        unlockAndAddIntent.putExtra(MessagesActivity.CAMERA_NAME, cameraName);
        unlockAndAddIntent.putExtra(MessagesActivity.SNAPSHOT_ID, snapshotId);
        unlockAndAddIntent.putExtra(MessagesActivity.ADD, "1");
        unlockAndAddIntent.putExtra(MessagesActivity.NOTIFICATION_ID, notificationId);
        builder.addAction(
                android.R.drawable.ic_menu_add,
                context.getString(R.string.unlock_and_add),
                PendingIntent.getActivity(context, 0, unlockAndAddIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        );

        return builder;
    }
}
