package com.github.aamnony.smartdoorbell;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;


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
                    if (!person.getSnapshotId().equals("null")) {
                        try {
                            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                                    getApplicationContext(),
                                    AppHelper.IDENTITIES_POOL_ID,
                                    AppHelper.IOT_REGION
                            );
                            AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);
                            s3Client.setRegion(Region.getRegion(AppHelper.COGNITO_REGION));

                            // Set the presigned URL to expire after one hour.
                            Date expiration = new Date();
                            long expTimeMillis = expiration.getTime();
                            expTimeMillis += 1000 * 60 * 5;
                            expiration.setTime(expTimeMillis);

                            // Generate the presigned URL.
                            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                                    new GeneratePresignedUrlRequest(AppHelper.S3_BUCKET_NAME, person.getSnapshotId())
                                            .withMethod(HttpMethod.GET)
                                            .withExpiration(expiration);
                            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
                            person.setSnapshotTempUrl(url.toString());
                        } catch (AmazonServiceException e) {
                            // The call was transmitted successfully, but Amazon S3 couldn't process
                            // it, so it returned an error response.
                            e.printStackTrace();
                        }
                    }
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



