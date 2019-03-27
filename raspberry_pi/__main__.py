import time

import boto3

import firebase_admin
import firebase_admin.credentials
import firebase_admin.messaging


USER_NAME = 'nssl'
CAMERA_NAME = 'frontdoor'

BUCKET_NAME = 'smartdoorbellfaces'
FCM_CONFIG_PATH = 'fcm.json'
ROOT_BUCKET_FOLDER = USER_NAME + '/' + CAMERA_NAME
RECOGNIZED_FACES_BUCKET_FOLDER = ROOT_BUCKET_FOLDER + '/recognized/'
LOG_FACES_BUCKET_FOLDER = ROOT_BUCKET_FOLDER + '/log/'
IMAGE_EXTENSION = 'jpg'
    

def publish_message(image_name: str) -> None:
    message = firebase_admin.messaging.Message(
        data={
            'image_name': image_name,
            'camera_name': CAMERA_NAME,
        },
        topic=USER_NAME,
    )
    response = firebase_admin.messaging.send(message)
    print('Successfully sent message:', response)  # TODO: remove?

def upload_image(local_path: str, image_name: str) -> None:
    s3 = boto3.client('s3')
    s3.upload_file(local_path, BUCKET_NAME, image_name)

def download_image(local_path: str, image_name: str) -> None:
    s3 = boto3.resource('s3')
    s3.Bucket(BUCKET_NAME).download_file(image_name, local_path)

def debug_create_local_image(local_path: str):
    # TODO: Delete this function.
    import shutil
    shutil.copyfile('Malta3-Pentasia-800x400.jpg', local_path)


fcm_credentials = firebase_admin.credentials.Certificate(FCM_CONFIG_PATH)
fcm_app = firebase_admin.initialize_app(fcm_credentials)

local_path = str(time.time()) + '.' + IMAGE_EXTENSION
# image_name = LOG_FACES_BUCKET_FOLDER + local_path
image_name = LOG_FACES_BUCKET_FOLDER + '1553703251.4841993.jpg'

# debug_create_local_image(local_path) # TODO: Temporary, remove.

# upload_image(local_path, image_name)
publish_message(image_name)
