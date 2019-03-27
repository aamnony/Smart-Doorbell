package com.github.aamnony.smartdoorbell;

import android.os.Bundle;

import org.jitsi.meet.sdk.JitsiMeetActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class StreamActivity extends JitsiMeetActivity {

    public static final String ROOM_NAME = "room_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String roomUrl = AppHelper.STREAM_SERVER_URL + getIntent().getStringExtra(ROOM_NAME) +
                "#config.startWithVideoMuted=true" + "&config.startWithAudioMuted=true";
        try {
            loadURL(new URL(roomUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
