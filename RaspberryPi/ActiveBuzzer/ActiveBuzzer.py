import sys
import RPi.GPIO as GPIO
import time

BuzzerPin = 4

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(BuzzerPin, GPIO.OUT, initial=GPIO.LOW)

while True:
	#if humidity is not None and temperature is not None:
        GPIO.output(BuzzerPin, GPIO.HIGH)
        time.sleep(5)
        GPIO.cleanup()
        sys.exit()
                #GPIO.output(DHTPin, GPIO.LOW)
	#else:
        #	print("Failed to retrieve data from sensor")