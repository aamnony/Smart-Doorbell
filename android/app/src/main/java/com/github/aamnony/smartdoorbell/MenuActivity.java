package com.github.aamnony.smartdoorbell;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    private Button btnLog;
    private Button btnActions;
    private Button btnCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        btnLog = findViewById(R.id.btnLog);
        btnLog.setOnClickListener(OnClickedLog);
        btnActions = findViewById(R.id.btnActions);
        btnActions.setOnClickListener(OnClickedActions);
        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(OnClickedCamera);

    }

    private View.OnClickListener OnClickedLog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent streamActivityIntent = new Intent(MenuActivity.this, LogActivity.class);
            startActivity(streamActivityIntent);
        }
    };


    private View.OnClickListener OnClickedActions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent streamActivityIntent = new Intent(MenuActivity.this, MessagesActivity.class);
            startActivity(streamActivityIntent);
        }
    };

    private View.OnClickListener OnClickedCamera = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent streamActivityIntent = new Intent(MenuActivity.this, StreamActivity.class);
            startActivity(streamActivityIntent);
        }
    };
}
