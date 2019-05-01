import time

import AWSIoTPythonSDK
import AWSIoTPythonSDK.MQTTLib

import boto3
import botocore
import botocore.exceptions

import cv2

import firebase_admin
import firebase_admin.credentials
import firebase_admin.messaging


# Device Parameters:
USER_NAME                   = 'nssl'
CAMERA_NAME                 = 'frontdoor'
SHOW_CAMERA_WINDOW          = True
DEFAULT_UNLOCK_DURATION     = 5
STREAM_KEEP_ALIVE_TIMEOUT   = 5
LOG_LEVEL                   = 1  # Lower value means more verbose messages will be displayed

# Framework Parameters:
BUCKET_NAME                     = 'smartdoorbellfaces'
FCM_CONFIG_PATH                 = 'fcm.json'
ROOT_BUCKET_FOLDER              = USER_NAME + '/' + CAMERA_NAME
RECOGNIZED_FACES_BUCKET_FOLDER  = ROOT_BUCKET_FOLDER + '/recognized/'
LOG_FACES_BUCKET_FOLDER         = ROOT_BUCKET_FOLDER + '/log/'
IMAGE_EXTENSION                 = '.jpeg'
REKOGNITION_COLLECTION_ID       = 'SmartDoorbell'
MQTT_ENDPOINT                   = 'a18gejfspwq80u.iot.us-east-1.amazonaws.com'
MQTT_ROOT_CERTIFICATE_PATH      = 'root-CA.crt'
MQTT_PRIVATE_KEY_PATH           = 'ab6bcc17f5-private.pem.key'
MQTT_CERTIFICATE_PATH           = 'ab6bcc17f5-certificate.pem.crt'
ACTION_UNLOCK_DOOR              = 'open'
ACTION_STREAM                   = 'stream'


def log(*values: any, level: int = LOG_LEVEL) -> None:
    if level >= LOG_LEVEL:
        print('<', USER_NAME, '/', CAMERA_NAME, '> ', *values, sep='', flush=True)


class Messaging:
    @staticmethod
    def init_fcm():
        fcm_credentials = firebase_admin.credentials.Certificate(FCM_CONFIG_PATH)
        fcm_app = firebase_admin.initialize_app(fcm_credentials)

    @staticmethod
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

    @staticmethod
    def get_mqtt_client() -> AWSIoTPythonSDK.MQTTLib.AWSIoTMQTTClient:
        mqtt_client = AWSIoTPythonSDK.MQTTLib.AWSIoTMQTTClient(USER_NAME + '/' + CAMERA_NAME)
        mqtt_client.disableMetricsCollection()
        mqtt_client.configureEndpoint(MQTT_ENDPOINT, 8883)
        mqtt_client.configureCredentials(MQTT_ROOT_CERTIFICATE_PATH, MQTT_PRIVATE_KEY_PATH, MQTT_CERTIFICATE_PATH)

        mqtt_client.configureOfflinePublishQueueing(-1)     # Infinite offline Publish queueing
        mqtt_client.configureDrainingFrequency(2)           # Draining: 2 Hz
        mqtt_client.configureConnectDisconnectTimeout(10)   # 10 sec
        mqtt_client.configureMQTTOperationTimeout(5)        # 5 sec

        return mqtt_client


class FaceRecognition:
    @staticmethod
    def upload_image(image_bytes: bytes, image_name: str) -> None:
        log('Uploading image to bucket/', image_name, level=1)
        s3 = boto3.client('s3')
        s3.put_object(
            Bucket=BUCKET_NAME,
            Body=image_bytes,
            Key=image_name
        )

    @staticmethod
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
        self.last_stream_request_time = 0

        self.camera = cv2.VideoCapture(0)
        self.face_cascade = cv2.CascadeClassifier('/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml')

        self.mqtt_client = Messaging.get_mqtt_client()

    def run(self) -> None:
        if SHOW_CAMERA_WINDOW:
            log('Showing camera')
            cv2.namedWindow('camera', cv2.WINDOW_AUTOSIZE)
            cv2.startWindowThread()

        self.mqtt_client.connect()
        self.mqtt_client.subscribe(USER_NAME + '/' + CAMERA_NAME, 1, self.on_message_received)
        log('Ready')
        while True:
            self.step()
        self.mqtt_client.disconnect()

    def goto_face_detecting(self) -> None:
        self.state = self.FACE_DETECTING
        if not self.camera.isOpened():
            self.camera.open(0)

    def goto_streaming(self) -> None:
        self.state = self.STREAMING
        if self.camera.isOpened():
            self.camera.release()
        # TODO: open room in browser

    def on_message_received(self, client, userdata, message) -> None:
        action = str(message.payload.decode('utf-8'))
        log(action)
        if action == ACTION_UNLOCK_DOOR:
            self.received_open_request = True
        elif action == ACTION_STREAM:
            self.received_stream_request = True
            self.last_stream_request_time = time.time()

    def step(self) -> None:
        if self.received_open_request:
            self.received_open_request = False
            self.door.unlock()
            self.goto_face_detecting(self)

        elif self.state == self.STREAMING:
            if time.time() - self.last_stream_request_time < STREAM_KEEP_ALIVE_TIMEOUT:
                # Stream was closed by user.
                self.received_stream_request = False
                self.goto_face_detecting()

        elif self.state == self.FACE_DETECTING:
            if self.received_stream_request:
                self.goto_streaming()
                return

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
                FaceRecognition.upload_image(image_bytes, image_name)

                self.camera.release()

                if FaceRecognition.is_face_recognized(image_name):
                    Door.unlock()
                else:
                    Messaging.publish_message(image_name)

                self.camera.open(0)



StateMachine().run()
