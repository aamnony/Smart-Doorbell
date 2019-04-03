import time

import boto3
import botocore
import botocore.exceptions

import cv2

import firebase_admin
import firebase_admin.credentials
import firebase_admin.messaging


USER_NAME = 'nssl'
CAMERA_NAME = 'frontdoor'

SHOW_CAMERA_WINDOW = True
DEFAULT_UNLOCK_DURATION = 5

LOG_LEVEL = 1  # Lower value means more verbose messages will be displayed

BUCKET_NAME = 'smartdoorbellfaces'
FCM_CONFIG_PATH = 'fcm.json'
ROOT_BUCKET_FOLDER = USER_NAME + '/' + CAMERA_NAME
RECOGNIZED_FACES_BUCKET_FOLDER = ROOT_BUCKET_FOLDER + '/recognized/'
LOG_FACES_BUCKET_FOLDER = ROOT_BUCKET_FOLDER + '/log/'
IMAGE_EXTENSION = '.jpeg'
REKOGNITION_COLLECTION_ID = 'SmartDoorbell'


def log(*values: any, level: int = LOG_LEVEL) -> None:
    if level >= LOG_LEVEL:
        print('<', USER_NAME, '/', CAMERA_NAME, '> ', *values, sep='', flush=True)

def upload_image_file(local_path: str, image_name: str) -> None:
    s3 = boto3.client('s3')
    s3.upload_file(local_path, BUCKET_NAME, image_name)
    
def download_image_file(local_path: str, image_name: str) -> None:
    s3 = boto3.resource('s3')
    s3.Bucket(BUCKET_NAME).download_file(image_name, local_path)

def debug_create_local_image(local_path: str):
    # TODO: Delete this function.
    import shutil
    shutil.copyfile('Malta3-Pentasia-800x400.jpg', local_path)

def publish_message(image_name: str) -> None:
    message = firebase_admin.messaging.Message(
        data={
            'image_name': image_name,
            'camera_name': CAMERA_NAME,
        },
        topic=USER_NAME,
    )
    response = firebase_admin.messaging.send(message)
    log('Successfully sent message: ', response, level=1)

def upload_image(image_bytes: bytes, image_name: str) -> None:
    log('Uploading image to bucket/', image_name, level=1)
    s3 = boto3.client('s3')
    s3.put_object(
        Bucket=BUCKET_NAME,
        Body=image_bytes,
        Key=image_name
    )

def is_face_recognized(image_name: str) -> bool:
    log('Running rekognition on image from bucket/', image_name, level=1)
    rekognition = boto3.client('rekognition')
    try:
        response = rekognition.search_faces_by_image(
            CollectionId=REKOGNITION_COLLECTION_ID,
            Image={
                'S3Object': {
                    'Bucket': BUCKET_NAME,
                    'Name': image_name,
                }
            },
            MaxFaces=1
        )
        
        if len(response['FaceMatches']) > 0:
            log(response, level=0)
            log('Recognized! It is ', response['FaceMatches'][0]['Face']['ExternalImageId'])
            return True
        else:
            log('Unrecognized')
            return False
    except botocore.exceptions.ClientError:
        log('Unrecognized')
        return False  # False-positive: no faces detected in image.

class Door:
    @classmethod
    def unlock(cls, duration: int = DEFAULT_UNLOCK_DURATION) -> None:
        """
        Unlock the door for a given duration (in seconds).
        If the duration is non-positive, unlock the door indefinitely (until a lock command is sent).
        """
        # TODO: send open to GPIO
        log('Unlocking the door')
        if duration > 0:
            time.sleep(duration)
            cls.lock(0)

    @classmethod
    def lock(cls, duration: int = 0) -> None:
        """
        Lock the door for a given duration (in seconds).
        If the duration is non-positive, lock the door indefinitely (until an unlock command is sent).
        """
        # TODO: send close to GPIO
        log('Locking the door')
        if duration > 0:
            time.sleep(duration)
            cls.unlock(0)


class StateMachine:
    STREAMING = 0
    FACE_DETECTING = 1
    
    def __init__(self) -> None:
        self.state = self.FACE_DETECTING
        self.received_stream_request = False
        self.received_open_request = False

        self.camera = cv2.VideoCapture(0)
        self.face_cascade = cv2.CascadeClassifier('/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml')

    def run(self) -> None:
        if SHOW_CAMERA_WINDOW:
            log('Showing camera')
            cv2.namedWindow('camera', cv2.WINDOW_AUTOSIZE)
            cv2.startWindowThread()
            
        log('Ready')
        while True:
            self.step()

    def goto_face_detecting(self) -> None:
        self.state = self.FACE_DETECTING
        if not self.camera.isOpened():
            self.camera.open(0)
        
    def goto_streaming(self) -> None:
        self.state = self.STREAMING
        if self.camera.isOpened():
            self.camera.release()
        # TODO: open room in browser
        
    def step(self) -> None:
        if self.received_stream_request:
            self.goto_streaming()
            return
        if self.received_open_request:
            self.door.unlock()
            self.goto_face_detecting(self)
            return
        
        if self.state == self.STREAMING:
            if not self.received_stream_request:
                self.goto_face_detecting()
                return
                
        elif self.state == self.FACE_DETECTING:
            rc, image = self.camera.read()
            image = cv2.resize(image, (320, 240))
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(gray, 1.15, 3)

            if SHOW_CAMERA_WINDOW:
                cv2.imshow('camera', image)

            if len(faces) > 0:
                log('Face detected')
                _, encoded_image = cv2.imencode(IMAGE_EXTENSION, image)
                
                image_bytes = encoded_image.tobytes()
                image_name = LOG_FACES_BUCKET_FOLDER + str(time.time()) + IMAGE_EXTENSION
                upload_image(image_bytes, image_name)

                self.camera.release()
                
                if is_face_recognized(image_name):
                    Door.unlock()
                elif False:  # TODO
                    fcm_credentials = firebase_admin.credentials.Certificate(FCM_CONFIG_PATH)
                    fcm_app = firebase_admin.initialize_app(fcm_credentials)

                    local_path = str(time.time()) + IMAGE_EXTENSION
                    # image_name = LOG_FACES_BUCKET_FOLDER + local_path
                    image_name = LOG_FACES_BUCKET_FOLDER + '1553703251.4841993.jpg'

                    # debug_create_local_image(local_path) # TODO: Temporary, remove.

                    # upload_image(local_path, image_name)
                    publish_message(image_name)
                    
                self.camera.open(0)

            

StateMachine().run()
