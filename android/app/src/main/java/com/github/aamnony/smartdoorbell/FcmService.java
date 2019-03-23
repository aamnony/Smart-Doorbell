package com.github.aamnony.smartdoorbell;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        FcmNotification.post(remoteMessage.getData(), FcmService.this);
    }
}
