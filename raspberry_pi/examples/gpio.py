import time

import RPi.GPIO

RPi.GPIO.setmode(RPi.GPIO.BCM)

#RPi.GPIO.setup(26, RPi.GPIO.OUT)

#RPi.GPIO.output(26, RPi.GPIO.LOW)
#time.sleep(5)
BELL_PIN_NUMBER = 19
RPi.GPIO.setup(BELL_PIN_NUMBER, RPi.GPIO.IN, pull_up_down=RPi.GPIO.PUD_DOWN)
def my_callback(channel):
    if RPi.GPIO.input(BELL_PIN_NUMBER) == RPi.GPIO.HIGH:
        print('asasdasdasd')



RPi.GPIO.add_event_detect(BELL_PIN_NUMBER, RPi.GPIO.RISING, callback=my_callback, bouncetime=300)
try:
    while True:
        pass
except KeyboardInterrupt:
    RPi.GPIO.cleanup()
