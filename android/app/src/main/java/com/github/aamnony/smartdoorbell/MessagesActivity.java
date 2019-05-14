package com.github.aamnony.smartdoorbell;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MessagesActivity extends AppCompatActivity {

    private static final String TAG = MessagesActivity.class.getCanonicalName();
    public static final String SNAPSHOT_ID = "snapshot_id";
    public static final String CAMERA_NAME = "camera_name";
    private String cameraName;
    private String snapshotId;
    private Button btnAccept;
    private Button btnReject;
    private Button btnVideo;
    private Mqtt mqtt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(AcceptClick);
        btnReject = findViewById(R.id.btnReject);
        btnReject.setOnClickListener(RejectClick);
        btnVideo = findViewById(R.id.btnVideo);
        btnVideo.setOnClickListener(VideoClick);

        mqtt = Mqtt.get(this);
        cameraName = getIntent().getStringExtra(CAMERA_NAME);
        if (cameraName == null) {
            cameraName = "frontdoor"; // TODO:
        }
        snapshotId = getIntent().getStringExtra(SNAPSHOT_ID);
        if (snapshotId != null) {
            showNewPersonDialog(this);
        }
    }
    
    private View.OnClickListener AcceptClick = v -> mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR);

    private View.OnClickListener RejectClick = v -> Toast.makeText(MessagesActivity.this, "reject", Toast.LENGTH_SHORT).show();

    private View.OnClickListener VideoClick = v -> startActivity(new Intent(MessagesActivity.this, StreamActivity.class));

    private void showAddItemDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Add a new person to DataBase")
                .setMessage("Please Insert Person's Full Name")
                .setView(taskEditText)
                .setPositiveButton("Add", (dialog1, which) -> {
                    String name = String.valueOf(taskEditText.getText());
                    mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR, name, snapshotId);
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showNewPersonDialog(Context c) {
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Do you want to add this person?")
                .setPositiveButton("Yes", (dialog1, which) -> showAddItemDialog(c))
                .setNegativeButton("No", (dialog12, which) -> {
                    mqtt.sendMessage(AppHelper.getCurrUser() + "/" + cameraName, Mqtt.ACTION_UNLOCK_DOOR);
                })
                .create();
        dialog.show();
    }

}


//send message

