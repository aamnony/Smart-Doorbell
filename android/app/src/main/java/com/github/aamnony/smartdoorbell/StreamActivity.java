package com.github.aamnony.smartdoorbell;

import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.jitsi.meet.sdk.JitsiMeetActivity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class StreamActivity extends JitsiMeetActivity {

    public static final String CAMERA_NAME = "camera_name";
    public static final String NOTIFICATION_ID = "notification_id";
    private String cameraName;
    private Mqtt mqtt;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int notificationId = getIntent().getIntExtra(NOTIFICATION_ID, -1);
        if (notificationId > -1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(notificationId);
        }

        cameraName = getIntent().getStringExtra(CAMERA_NAME);
        String roomUrl = AppHelper.STREAM_SERVER_URL + AppHelper.getCurrUser() + cameraName +
                "#config.startWithVideoMuted=true" + "&config.startWithAudioMuted=true";

        mqtt = Mqtt.get(this);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_STREAM);
            }
        }, 0, 5000);

        try {
            loadURL(new URL(roomUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stream, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_unlock:
                mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
