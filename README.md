# Concentration Assistant Using Android And RaspberryPi
This project involves the development of a concentration-enhancing device prototype consisting of an Android app, Raspberry Pi, and EEG sensor.
## Overview
The goal is to build a system that can help users stay focused on tasks by alerting them when concentration lapses. The core components are:

* Android App: Provides a simple interface to control device modes and block notifications/apps. Written in Java using Android Studio.
* Raspberry Pi: Handles sensor data processing and alert activation. Runs Python code to interface with EEG and control vibration motor.
* EEG Sensor: Measures brain waves to detect concentration levels. An affordable DIY EEG circuit built using various electronic components.
* Vibration Motor: Provides haptic feedback to alert user when concentration lapses. Controlled by Raspberry Pi over MQTT.
## Android App
The app allows users to select a timer duration and enables a "focus mode" which blocks access to other apps and notifications. This prevents distractions while the user is concentrating on a task.

Main Activities:

* MainActivity.java - Sets up the UI and contains callback methods for button clicks.
* FirstFragment.java - Manages the settings UI and mode selection.
* Overrides.java - Overrides for controlling back button behavior.
  
Key Functions:

* startOverdriveTimer() - Enables focus mode with no breaks.
* startPomodoroTimer() - Uses Pomodoro technique of timed intervals with breaks.
* lockApp() - Starts lock task mode to block app switching and notifications.
## Android App Video Demonstration

https://github.com/wilsonnexus/Concentration-Assistant-Android-RaspberryPi/assets/96637419/5e3b3ed0-58c7-4394-ae75-99553a734aad 

## Raspberry Pi
The Raspberry Pi processes the EEG data and controls the vibration motor over MQTT.

Key Files:

* MqttRasp.py - Subscribes to MQTT messages from the Android app and controls vibration motor.
* analysis_tools.py - Functions for processing EEG data and analyzing brain waves.
* childattention.py - Monitors EEG data and activates vibration alert when concentration lapses.
## EEG Sensor
The EEG sensor hardware uses an ADC chip along with various op-amps, resistors, and capacitors. Electrodes are placed on the scalp to measure brain waves.

* Circuit diagram and construction guide available in the thesis paper.
* Outputs analog voltage signals corresponding to alpha brain waves.
* Raspberry Pi uses ADC chip (ADS1015) to interface with the EEG circuit.
## EEG Raspberry Pi Video Demonstration

https://github.com/wilsonnexus/Concentration-Assistant-Android-RaspberryPi/assets/96637419/95f3bf90-a907-4a30-80f3-4981b158bcac

## Usage
To use the full system:

1. Build the EEG circuit and connect electrodes.
2. Connect EEG circuit output to Raspberry Pi.
3. Install Android app on phone.
4. Run MqttRasp.py and childattention.py on Raspberry Pi.
5. Select mode and start timer in Android app.
6. EEG data is monitored to detect lapses in concentration and trigger vibration motor.
Refer to the full thesis paper for additional details on the implementation, experimentation, and results. Comments in the code also provide insights.

## Future Work
* Improve EEG signal quality and motion artifacts filtering.
* Add more sensors for additional biometric data.
* Incorporate machine learning to customize experience per user.
* Develop a more robust and miniaturized hardware prototype.
* Study long-term impacts on productivity, cognitive abilities, etc.
* Expand features of app for scheduling, productivity tracking, etc.
* Detect whether concentration is on productive tasks vs distractions.
## Credits

By Wilson Neira

Original EEG code by Ryan Lopez

Thesis project for Honors College at UMass Amherst
