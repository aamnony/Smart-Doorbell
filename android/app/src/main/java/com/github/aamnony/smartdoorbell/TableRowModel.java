package com.github.aamnony.smartdoorbell;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "SmartDoorbellLogTable")
public class TableRowModel {
    private String personName;
    private Double timeStamp;
    private String actionType;
    private String snapshotId;
    private String userCamera;
    private String snapshotTempUrl;

    @DynamoDBAttribute(attributeName = "User/Camera")
    @DynamoDBHashKey(attributeName = "User/Camera")
    public String getUserCamera() {
        return userCamera;
    }

    public void setUserCamera(String userCamera) {
        this.userCamera = userCamera;
    }

    @DynamoDBAttribute(attributeName = "PersonName")
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @DynamoDBAttribute(attributeName = "Timestamp")
    public Double getTimeStamp() {return timeStamp;}

    public void setTimeStamp(Double timeStamp) {
        this.timeStamp = timeStamp;
    }

    @DynamoDBAttribute(attributeName = "ActionType")
    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    @DynamoDBAttribute(attributeName = "SnapshotId")
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }


    public String getSnapshotTempUrl() {
        return snapshotTempUrl;
    }

    public void setSnapshotTempUrl(String snapshotTempUrl) {
        this.snapshotTempUrl = snapshotTempUrl;
    }
}