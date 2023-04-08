from time import sleep
import os,sys
import RPi.GPIO as GPIO
import paho.mqtt.client as paho
from urllib.parse import urlparse
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
LED_PIN=10  #define LED pin
GPIO.setup(LED_PIN,GPIO.OUT, initial=GPIO.LOW)   # Set pin function as output


def on_connect(self, mosq, obj, rc):
        self.subscribe("led", 0)
    
def on_message(mosq, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
    if(msg.payload.decode('utf8') == str(1)):    
        print("LED on")      
        GPIO.output(LED_PIN,GPIO.HIGH)  #LED ON
    elif(msg.payload.decode('utf8') == str(0)):    
        print("LED off")
        GPIO.output(LED_PIN,GPIO.LOW)   # LED OFF

def on_publish(mosq, obj, mid):
    print("mid: " + str(mid))

    
def on_subscribe(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))


mqttc = paho.Client("WilsonAndroidClient", clean_session=False)

# Assign event callbacks
mqttc.on_message = on_message                          # called as callback
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish
mqttc.on_subscribe = on_subscribe


mqttc.username_pw_set("kigpggpf", "kb55FEKZNTjA")# "P4nf0uecWGOu"
mqttc.connect("driver.cloudmqtt.com", 18741, 60)
# Set a stopping condition or just press ctrl+C to quit.
"""while(True):
    inp = input()
    print(inp)
    mqttc.publish("sensor/temp", payload=inp, qos=0)"""
    
rc = 0
while True:
    while rc == 0:
        import time   
        rc = mqttc.loop()
        #time.sleep(0.5)
    print("rc: " + str(rc))
    if rc == 7:
        print("Connection lost, reconnecting...")
        mqttc.reconnect()
        mqttc.subscribe("led", 0)
        rc = 0
