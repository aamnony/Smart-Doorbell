package com.github.aamnony.smartdoorbell;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;


public class LogActivity extends AppCompatActivity {

    private DynamoDBMapper dynamoDBMapper;
    private ListView listLog;
    private Context curContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        listLog = findViewById(R.id.list_log);


        //AWSCredentials credentials = AmazonSharedPreferencesWrapper
        //       .getCredentialsFromSharedPreferences(this.sharedPreferences);

        //AmazonDynamoDBClient ddb = new AmazonDynamoDBClient(credentials);
        //DynamoDBMapper mapper = new DynamoDBMapper(ddb);


        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                ArrayList<TableRowModel> msgArrayList = new ArrayList<TableRowModel>(10);
                AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance());
                dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).build();
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

                PaginatedScanList<TableRowModel> resultList = dynamoDBMapper.scan(TableRowModel.class, scanExpression);
                System.out.println(resultList.toString());

                for (TableRowModel person : resultList) {
                    System.out.println(person.toString());
                    msgArrayList.add(person);
                }
                TableRowModel[] msgArray = msgArrayList.toArray(new TableRowModel[0]);
                runOnUiThread(() -> listLog.setAdapter(new MsgAdapter(LogActivity.this, msgArray)));
                //listLog.setAdapter(new MsgAdapter(LogActivity.this,msgArray));
            }

            @Override
            public void onError(Exception e) {

            }
        });


    }

}



