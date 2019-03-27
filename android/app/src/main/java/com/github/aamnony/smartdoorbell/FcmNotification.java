package com.github.aamnony.smartdoorbell;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.util.Map;

public class FcmNotification {

    private static final int ID = 0xC2FF8;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static AmazonS3Client sS3Client;

    public static void post(Map<String, String> data, Context context) {
        NotificationCompat.Builder builder = buildNotification(data, context, createChannel(context));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ID, builder.build());
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

    private static NotificationCompat.Builder buildNotification(Map<String, String> data, Context context, String channel) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Unrecognized Person At Your Door");

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                AppHelper.IDENTITIES_POOL_ID,
                AppHelper.IOT_REGION
        );
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);
        s3Client.setRegion(Region.getRegion(AppHelper.COGNITO_REGION));

        String image_name = data.get("image_name");
        S3Object s3Object = s3Client.getObject(AppHelper.S3_BUCKET_NAME, image_name);
        S3ObjectInputStream content = s3Object.getObjectContent();
        Bitmap bmp = BitmapFactory.decodeStream(content);
        new NotificationCompat.BigPictureStyle().bigPicture(bmp).setBuilder(builder);

        Intent streamIntent = new Intent(context, StreamActivity.class);
        streamIntent.putExtra(StreamActivity.EXTRA_USER_NAME, data.get(StreamActivity.EXTRA_USER_NAME));
        builder.addAction(android.R.drawable.presence_video_online, "View Stream", PendingIntent.getActivity(context, ID, streamIntent, 0));

        // Add onClick behaviour.
//        builder.setContentIntent(PendingIntent.getActivity(context, ID, streamIntent, 0));

        return builder;
    }
}
