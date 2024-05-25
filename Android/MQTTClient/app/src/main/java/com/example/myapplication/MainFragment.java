package com.example.myapplication;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.helper.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener {
    private Button disable;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName componentName;
    MqttHelper mqttHelper;
    TextView dataReceived;
    private boolean mIsLocked = false;
    // Allowlist one app
    private static final String KIOSK_PACKAGE = "com.example.myapplication";
    private CountDownTimer timer;
    private Spinner durationSpinner;
    private TextView remainingTimeTextView;
    private Vibrator vibrator;

    // Default constructor with no dependencies
    public MainFragment() {
    }

    // Constructors for dependencies
    public MainFragment(DevicePolicyManager devicePolicyManager, MqttHelper mqttHelper, Vibrator vibrator) {
        this.devicePolicyManager = devicePolicyManager;
        this.mqttHelper = mqttHelper;
        this.vibrator = vibrator;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        devicePolicyManager = (DevicePolicyManager) requireContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        componentName = new ComponentName(requireContext(), Controller.class);
        startMqtt();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disable = (Button) view.findViewById(R.id.disable);
        durationSpinner = view.findViewById(R.id.durationSpinner);
        // Get the Vibrator service
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Convert values to hours and minutes
        String[] durations = getResources().getStringArray(R.array.durations_array);
        List<String> durationsList = new ArrayList<>();
        for (String duration : durations) {
            int durationValue = Integer.parseInt(duration);
            if (durationValue == 1) {
                durationsList.add(duration + " minute");
            }
            else if (durationValue >= 60) {
                durationsList.add((durationValue / 60) +
                        ((durationValue < 120) ? " hour " : " hours ") +
                        (((durationValue % 60) <= 1) ? "" : (durationValue - 60) + " minutes"));
            } else {
                durationsList.add(duration + " minutes");
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.custom_spinner_item, durationsList);

        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        durationSpinner.setAdapter(adapter);
        Log.d("debugger",durationSpinner.getSelectedItem().toString());
        // Set test selection
        //durationSpinner.setSelection(1);

        disable.setOnClickListener(this);
        remainingTimeTextView = view.findViewById(R.id.textView);

        dataReceived = (TextView) view.findViewById(R.id.textview_second);

        Button nextButton = view.findViewById(R.id.settings_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                ((MainActivity) requireActivity()).setCurrentFragmentSettings();
                navController.navigate(R.id.action_mainFragment_to_secondFragment);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == disable) {
            if (!((MainActivity) requireActivity()).getOverdrive()) {
                pomodoroTechnique();
            }
            else {
                overdriveImplementation();
            }

        }

    }

    public Spinner getDurationSpinner() {
        return durationSpinner;
    }

    public Button getDisableButton() {
        return disable;
    }

    public Vibrator getVibrator() {
        return vibrator;
    }

    public void overdriveImplementation() {
        if (!mIsLocked) {
            // First click
            int delayDuration = Integer.parseInt(durationSpinner.getSelectedItem().toString().replaceAll("[^\\d.]", "")) * 60000;

            // Update a TextView to show the time remaining
            // Execute first action when timer is done
            lockApp();

            // Create a new timer for 5 seconds (adjust as desired)
            timer = new CountDownTimer(delayDuration, 1000) {
                public void onTick(long millisUntilFinished) {
                    // Update a TextView to show the time remaining
                    // (optional)
                    long remainingTime = millisUntilFinished / 1000;
                    remainingTimeTextView.setText("Time Left: " + timeToString(remainingTime));
                }

                public void onFinish() {
                    // Execute second action when timer is done
                    disable.setText("Disable");
                    ((MainActivity) requireActivity()).stopLock();
                    //stopLockTask();
                    mIsLocked = false;
                    remainingTimeTextView.setText("");
                }
            };
            timer.start(); // Start the new timer
        } else {

            // Second click
            disable.setText("Disable");
            timer.cancel();
            ((MainActivity) requireActivity()).stopLock();
            //stopLockTask();
            mIsLocked = false;
            remainingTimeTextView.setText("0");
            // Turn vibration motor on
            mqttHelper.publishToTopic("sensor/temp", "1");
            vibrator.vibrate(5000);

            // Turn vibration motor off
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mqttHelper.publishToTopic("sensor/temp", "0");
                }
            }, 5000);
            // mqttHelper.publishToTopic("sensor/temp", "0");
        }
    }

    public void pomodoroTechnique() {
        if (!mIsLocked) {
            // First click
            int workDuration = Integer.parseInt(durationSpinner.getSelectedItem().toString().replaceAll("[^\\d.]", "")) * 60000;
            int breakDuration = 5000; // Set your desired break duration in milliseconds

            // Lock the app (blocking)
            lockApp();
            if (workDuration / 60000 <= 5) {
                // Create a new timer for 5 seconds (adjust as desired)
                timer = new CountDownTimer(workDuration, 1000) {
                    public void onTick(long millisUntilFinished) {
                        // Update a TextView to show the time remaining
                        // (optional)
                        long remainingTime = millisUntilFinished / 1000;
                        remainingTimeTextView.setText("Time Left: " + timeToString(remainingTime));
                    }

                    public void onFinish() {
                        // Execute second action when timer is done
                        disable.setText("Disable");
                        ((MainActivity) requireActivity()).stopLock();
                        //stopLockTask();
                        mIsLocked = false;
                        remainingTimeTextView.setText("");
                    }
                };
            } else {
                // Create a new timer for the work period
                timer = new CountDownTimer(workDuration, 1000) {
                    public void onTick(long millisUntilFinished) {
                        long remainingTime = millisUntilFinished / 1000;
                        remainingTimeTextView.setText("Time Left: " + timeToString(remainingTime));
                        if(remainingTime / 60 % 5 == 0 && mIsLocked && remainingTime != 0) {
                            unlockApp();
                        }
                        else if(remainingTime / 60 % 5 == 0 && !mIsLocked && remainingTime != 0) {
                            lockApp();
                        }
                    }
                    public void onFinish() {
                        // Execute second action when timer is done
                        disable.setText("Disable");
                        ((MainActivity) requireActivity()).stopLock();
                        //stopLockTask();
                        mIsLocked = false;
                        remainingTimeTextView.setText("");
                    }
                };
            }
            timer.start(); // Start the work timer
        } else {
            // Second click
            unlockApp();
            timer.cancel();
            remainingTimeTextView.setText("0");
        }
    }

    private String timeToString(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        String timeResult = "";

        if (hours > 0) {
            timeResult += hours + ((hours == 1) ? " hour " : " hours ");
        }

        if (minutes > 0) {
            timeResult += minutes + ((minutes == 1) ? " minute " : " minutes ");
        }

        if (remainingSeconds > 0) {
            timeResult += remainingSeconds + ((remainingSeconds == 1) ? " second" : " seconds");
        }

        return timeResult.trim();
    }
    private void lockApp() {
        disable.setText("Cancel");
        boolean active = devicePolicyManager.isAdminActive(componentName);
        if (true) {
            ((MainActivity) requireActivity()).startLock();
            //startLockTask();
            mIsLocked = true;
        }
    }

    private void unlockApp() {
        disable.setText("Disable");
        ((MainActivity) requireActivity()).stopLock();
        //stopLockTask();
        mIsLocked = false;
        remainingTimeTextView.setText("");
        // Turn vibration motor on
        mqttHelper.publishToTopic("sensor/temp", "1");
        vibrator.vibrate(5000);

        // Turn vibration motor off
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mqttHelper.publishToTopic("sensor/temp", "0");
            }
        }, 5000);
    }

    public DevicePolicyManager getDevicePolicyManager() {
        return devicePolicyManager;
    }




    // Start MQTT Client Code
    public void startMqtt(){
        mqttHelper = new MqttHelper(requireContext().getApplicationContext());
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug","Connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
                //mChart.addEntry(Float.valueOf(mqttMessage.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

}