package com.github.aamnony.smartdoorbell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MessagesActivity1 extends AppCompatActivity {
    public static final String ADD = "add";
    public static final String SNAPSHOT_ID = "snapshot_id";
    public static final String CAMERA_NAME = "camera_name";
    public static final String NOTIFICATION_ID = "notification_id";
    private String cameraName;
    private String snapshotId;
    private Mqtt mqtt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        int notificationId = getIntent().getIntExtra(NOTIFICATION_ID, -1);
        if (notificationId > -1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(notificationId);
        }

        Button btnUnlock = findViewById(R.id.btnUnlock);
        btnUnlock.setOnClickListener(onUnlockClicked);
        Button btnStream = findViewById(R.id.btnStream);
        btnStream.setOnClickListener(onStreamClicked);

        mqtt = Mqtt.get(this);
        cameraName = getIntent().getStringExtra(CAMERA_NAME);
        if (cameraName == null) {
            cameraName = "frontdoor"; // TODO:
        }
        snapshotId = getIntent().getStringExtra(SNAPSHOT_ID);
        if (snapshotId != null) {
//            Toast.makeText(this, "snapshotID=" + snapshotId, Toast.LENGTH_SHORT).show();
            String add = getIntent().getStringExtra(ADD);
            if (add.equals("1")) {
//                Toast.makeText(this, "ADD = TRUE", Toast.LENGTH_SHORT).show();
                showAddPersonDialog();
            } else {
//                Toast.makeText(this, add, Toast.LENGTH_SHORT).show();
                mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR);
//                finish();
            }
        }
    }
    
    private View.OnClickListener onUnlockClicked = v -> mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR);

    private View.OnClickListener onStreamClicked = v -> {
        Intent streamIntent = new Intent(MessagesActivity1.this, StreamActivity.class);
        streamIntent.putExtra(StreamActivity.CAMERA_NAME, cameraName);
        startActivity(streamIntent);
    };

    private void showAddPersonDialog() {
        final EditText taskEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add a new person to the database")
                .setMessage("Please insert person's name without spaces")
                .setView(taskEditText)
                .setPositiveButton("Add", (dialog1, which) -> {
                    String name = String.valueOf(taskEditText.getText());
                    mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR, name, snapshotId);
//                    finish();
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
}
