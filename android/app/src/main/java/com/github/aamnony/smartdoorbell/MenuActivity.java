package com.github.aamnony.smartdoorbell;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Button btnLog = findViewById(R.id.btnLog);
        btnLog.setOnClickListener(onClickedLog);
        Button btnActions = findViewById(R.id.btnActions);
        btnActions.setOnClickListener(onClickedActions);
    }

    private View.OnClickListener onClickedLog = v -> {
        startActivity(new Intent(MenuActivity.this, LogActivity.class));
    };


    private View.OnClickListener onClickedActions = v -> {
        startActivity(new Intent(MenuActivity.this, MessagesActivity.class));
    };
}
