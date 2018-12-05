package com.github.aamnony.smartdoorbell;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "TestTable")
public class TableRowModel {
    private String personId;
    private String age;

    @DynamoDBHashKey(attributeName = "PersonId")
    @DynamoDBAttribute(attributeName = "PersonId")
    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    //    @DynamoDBRangeKey(attributeName = "Age")
    @DynamoDBAttribute(attributeName = "Age")
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "TableRowModel{" + "\n" +
                "personId='" + personId + '\'' + "\n" +
                "age='" + age + '\'' + "\n" +
                '}';
    }
}