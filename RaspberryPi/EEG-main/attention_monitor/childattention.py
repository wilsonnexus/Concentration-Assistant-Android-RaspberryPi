"""
Original Code by Ryan Lopez
Updated deprecated libraries and Modified Code to suit my project needs by Wilson Neira

Program Description:

This program beeps when the users alpha waves have been in the relaxed state for too long, with potential appications
to keep a user's attention.
"""

import os
import sys
sys.path.insert(1, os.path.dirname(os.getcwd())) #This allows importing files from parent folder
import time
from time import sleep
import RPi.GPIO as GPIO
import paho.mqtt.client as paho
from urllib.parse import urlparse
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
import board
import busio
import adafruit_ads1x15.ads1015 as ADS
from adafruit_ads1x15.analog_in import AnalogIn
from adafruit_ads1x15.ads1x15 import Mode
#from Adafruit_Python_ADS1x15.Adafruit_ADS1x15 import ADS1x15
#from Adafruit import ADS1x15
import pickle
import collections
from analysis_tools import get_power_spectrum, get_rms_voltage, get_brain_wave, get_cutoff
import pygame
from pygame import mixer #modulo for playing audio
mixer.init() 


LED_PIN0=10  #define LED pin
LED_PIN1=17
LED_PIN2=27 
GPIO.setup(LED_PIN0,GPIO.OUT, initial=GPIO.LOW)   # Set pin function as output
GPIO.setup(LED_PIN1,GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(LED_PIN2,GPIO.OUT, initial=GPIO.LOW)

def on_connect(self, mosq, obj, rc):
        self.subscribe("sensor/temp", 0)
    
def on_message(mosq, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
    if(msg.payload.decode('utf8') == str(1)):    
        print("LED on")      
        GPIO.output(LED_PIN0,GPIO.HIGH)  #LED ON
        GPIO.output(LED_PIN1,GPIO.HIGH)
        GPIO.output(LED_PIN2,GPIO.HIGH)
    elif(msg.payload.decode('utf8') == str(0)):    
        print("LED off")
        GPIO.output(LED_PIN0,GPIO.LOW)   # LED OFF
        GPIO.output(LED_PIN1,GPIO.LOW)
        GPIO.output(LED_PIN2,GPIO.LOW) 

def on_publish(mosq, obj, mid):
    print("mid: " + str(mid))

    
def on_subscribe(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

# Create an MQTT client and set up its callbacks
mqttc = paho.Client("WilsonEEGClient", clean_session=False)
# Assign event callbacks
mqttc.on_message = on_message                          # called as callback
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish
mqttc.on_subscribe = on_subscribe


mqttc.username_pw_set("kigpggpf", "kb55FEKZNTjA")# "P4nf0uecWGOu"
mqttc.connect("driver.cloudmqtt.com", 18741, 60)
mqttc.loop_start()


def update_timeseries(time_series, volt_diff):
    """
    Adds a new voltage difference measurement to the end of timeseries while deleting the first element to keep the same size.

    :param timeseries: Numpy array containing the most recent ADC voltage difference measurements
    """
    time_series = np.roll(time_series, -1)
    time_series[-1] = volt_diff
    return time_series

print()
print('Initializing')
print()
i2c = busio.I2C(board.SCL, board.SDA)
adc = ADS.ADS1015(i2c = i2c)
#adc = ADS1x15() #Instantiate Analog Digital Converter
VRANGE = 2/3 #Full range scale in mV. Options: 256, 512, 1024, 2048, 4096, 6144.
sps = 128 # Samples per second to collect data. Options: 128, 250, 490, 920, 1600, 2400, 3300.
sinterval = 1.0/sps
sampletime = 3 # how long to look back in time for current alpha waves
adc = ADS.ADS1015(i2c = i2c, gain = VRANGE, data_rate = sps, mode = Mode.SINGLE)
pygame.init()
clock = pygame.time.Clock() #clock is used to collect data and update graph on regular interval.

#ask for session length
while True:
    try:
        exptimem = float(input('Please enter session length in minutes (eg: 30)'))#ask session length in minutes
        exptime = int(60*exptimem)#convert to seconds
        print('Session time =', exptimem, 'minutes.')
        break
    except ValueError:
        print('Please enter a valid input. The input should be a number.')
            
print('Experiment time in seconds', exptime)
time_series_len = sampletime*sps
nsamples=exptime*sps
time_series = np.zeros(time_series_len)
freq = np.fft.fftfreq(time_series.size, d=1/sps) #Gets frequencies in Hz/sec for time_series
freq_min = 8 #minimum freq in Hz for alpha waves
freq_max = 12 #maximum freq in Hz for alpha waves

while True:
    response = input('Enter good cutoff voltage (number) or type c to calibrate (c).  ')
    if response == 'c':
        print("Wierd")
        # ADC Configuration
        
        #print(AnalogIn(adc, ADS.P2).voltage)
        #print(AnalogIn(adc, ADS.P3).voltage)
        #print(AnalogIn(adc, ADS.P2, ADS.P3).voltage)
        print("Wierd")
        cutoff = get_cutoff(3,5,490,adc, ADS)
        print('You have cutoff voltage',cutoff)
        break
    try:
        cutoff = float(response)
        break
    except:
        print('Please enter correct format input')
        
while True:
    try:
        max_rest = float(input('Enter maximum allowed rest time in seconds  '))
        break
    except:
        print('Please enter a number')

input('Press <Enter> to start %.1f minutes session... ' % exptimem)
print()

times = [] #fills with time values
rms_values = [] #fills with rms values
last_concentrate_dist = 0 #keeps track of how many indices ago the last concentrate was, if greater than max_rest*sps then sound alarm

#Initialize plot to be updated in real time
fig, ax = plt.subplots()
line, = ax.plot([],[], lw=3) #creates empty line object
ax.set_xlim(0, exptime)
ax.set_ylim(0,0.4)
ax.axhline(y=cutoff,color='black',label="Relaxed cutoff")
ax.set(xlabel='Time (s)', ylabel='RMS Alpha Wave Voltage (V)', title='Relaxation Level')
ax.legend()
fig.canvas.draw()
plt.show(block=False) #block=False shows plot and allows rest of code to run
time.sleep(3)
chan_diff = AnalogIn(adc, ADS.P2, ADS.P3)
#chan0 = AnalogIn(adc, ADS.P0, ADS.P1)
#chan1 = AnalogIn(adc, ADS.P2, ADS.P3)
#adc.startContinuousDifferentialConversion(2, 3, pga=VRANGE, sps=sps) #Returns the voltage difference in millivolts between port 2 and 3 on the ADC.
t0 = time.perf_counter()
for i in range(nsamples):
    st = time.perf_counter()
    #attempted to fix error after 1 minute
    #time.sleep(0.005)
    volt_diff = chan_diff.voltage-3.3
    print("volt_diff")
    print(volt_diff)
    print("volt_diff")
    #volt_diff = (chan0.voltage - chan1.voltage) * 1000
    #volt_diff = 0.001*adc.getLastConversionResults()-3.3 #0.001 to convert mV -> V, adc ground is 3.3 volts above circuit ground 
    time_series = update_timeseries(time_series, volt_diff)
    ps = get_power_spectrum(time_series)
    rms = get_rms_voltage(ps, freq_min, freq_max, freq, time_series_len)
    
    if rms > cutoff: #user in relaxed state:
        last_concentrate_dist +=1
    else: #user just concentrated
        last_concentrate_dist = 0
        mqttc.publish("sensor/temp", payload=0, qos=0, retain=False)
    if last_concentrate_dist > max_rest*sps:
        #user has been resting too long, alert them
        mqttc.publish("sensor/temp", payload=1, qos=0, retain=False)
    #Plotting real time stuff
    times.append(i*sinterval) #adds current time
    rms_values.append(rms) #adds rms
    line.set_data(times,rms_values) #updates the line element
    ax.draw_artist(line)
    fig.canvas.blit(ax.bbox)
    clock.tick(sps) #keeps loop from running at rate faster than sps
    
t = time.perf_counter() - t0
#adc.stopContinuousConversion()
print('Time elapsed: %.9f s.' % t)
print()
                                                   
                                                                                                         
