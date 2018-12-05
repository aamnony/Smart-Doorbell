package com.github.aamnony.smartdoorbell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.jitsi.meet.sdk.JitsiMeetActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoActivity extends JitsiMeetActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String roomUrl = null;
        try {
            loadURL(new URL(roomUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
