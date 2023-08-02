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

## Abstract  
The ability to concentrate on a task is crucial for productivity and success, yet it is a common struggle for many individuals. During my honors thesis, I aim to investigate the question "How can technology assist with concentration?" by exploring the potential of existing computer systems, such as smartphones and Raspberry Pis, to improve concentration and enhance weaker functioning related to concentration. As we increasingly rely on technology to augment our human capabilities, I believe that technology can play a significant role in supporting concentration. To this end, I proposed the development of a shoulder pad that transmits vibrations to a person when they have stopped focusing on a task, and a smartphone application that blocks distracting notifications and alerts the user when they want to take a break. In addition to these devices, I plan to measure concentration levels using EEG data collected through a DIY EEG sensor built with various materials. By working with these devices, I hope to gain insights into the underlying factors that contribute to reduced concentration levels and identify potential strategies for improving concentration. Ultimately, my thesis seeks to explore whether the development of such technology can aid the brain in concentrating on a task, and if not, what alternative approaches can be taken to support concentration. By shedding light on the complex nature of concentration and the potential for technology to assist in this area, I hope to contribute to the growing body of research on how technology can improve our cognitive abilities and enhance our overall well-being.

## Future Work
* Improve EEG signal quality and motion artifacts filtering.
* Add more sensors for additional biometric data.
* Incorporate machine learning to customize experience per user.
* Develop a more robust and miniaturized hardware prototype.
* Study long-term impacts on productivity, cognitive abilities, etc.
* Expand features of app for scheduling, productivity tracking, etc.
* Detect whether concentration is on productive tasks vs distractions.
Credits

By Wilson Neira

Original EEG code by Ryan Lopez

Thesis project for Honors College at UMass Amherst
