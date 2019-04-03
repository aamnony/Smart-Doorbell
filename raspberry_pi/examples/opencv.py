import time

import cv2


face_cascade = cv2.CascadeClassifier('/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml')

#Open the first webcame device
capture = cv2.VideoCapture(0)

#Create two opencv named windows
#cv2.namedWindow('camera', cv2.WINDOW_AUTOSIZE)

#Start the window thread for the two windows we are using
#cv2.startWindowThread()

is_face_detected = False
while True:
    #Retrieve the latest image from the webcam
    rc, img = capture.read()
    img = cv2.resize(img, (320, 240))
    #cv2.imshow('camera', img)

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.15, 3)

    if len(faces) > 0:
        if not is_face_detected:
            is_face_detected = True
            print('face detected')
    else:
        if is_face_detected:
            is_face_detected = False
