import time

import boto3


dynamodb = boto3.client('dynamodb')


item = dynamodb.get_item(
    TableName='SmartDoorbellLogTable',
    Key={
        'User/Camera': {
            'S': 'nssl/frontdoor'
        },
        'Timestamp': {
            'N': '1553702749.842719'
        }
          
    }
)
print(item)

# item = dynamodb.put_item(
    # TableName='SmartDoorbellLogTable',
    # Item={
        # 'User/Camera': {
            # 'S': 'nssl/frontdoor'
        # },
        # 'Timestamp': {
            # 'N': str(time.time())
        # },
        # 'ActionType': {
            # 'S': 'stream'
        # },
        # 'PersonName': {
            # 'S': 'null'
        # },
        # 'SnapshotId': {
            # 'S': 'null'
        # }
    # }
# )
# print(item)