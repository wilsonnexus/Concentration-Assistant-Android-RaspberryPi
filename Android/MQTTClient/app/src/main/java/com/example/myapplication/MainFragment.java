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
import androidx.navigation.ui.AppBarConfiguration;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.helper.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;


public class MainFragment extends Fragment implements View.OnClickListener {

    private Button lock, disable, enable;
    private static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName componentName;
    //public static boolean settingsClicked = false; // Declare boolean variable here
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    MqttHelper mqttHelper;

    TextView dataReceived;
    private Button btnPublish;
    private boolean overdrive = true;


    // Allowlist one app
    private static final String KIOSK_PACKAGE = "com.example.myapplication";
    private static final String[] APP_PACKAGES = {KIOSK_PACKAGE};
    private boolean backButtonEnabled = true;

    private boolean mIsLocked = false;
    private CountDownTimer timer;
    private Spinner durationSpinner;
    private TextView remainingTimeTextView;

    private Vibrator vibrator;
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
        // Replace the ArrayAdapter initialization code with the new code
        String[] durations = getResources().getStringArray(R.array.durations_array);
        List<String> durationsList = new ArrayList<>();
        for (String duration : durations) {
            int durationValue = Integer.parseInt(duration);
            if (durationValue == 1) {
                durationsList.add(duration + " minute");
            }
            else {
                durationsList.add(duration + " minutes");
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.custom_spinner_item, durationsList);


        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        durationSpinner.setAdapter(adapter);

        disable.setOnClickListener(this);
        remainingTimeTextView = view.findViewById(R.id.textView);
        //durationSpinner = view.findViewById(R.id.durationSpinner);

        dataReceived = (TextView) view.findViewById(R.id.textview_second);


        // Code Copied

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
            if (!overdrive) {
                pomodoroTechnique();
                //overdriveImplementation();
            }
            else {
                overdriveImplementation();
            }

        }

    }

    // Code Copied
    public void disableBackButton() {
        backButtonEnabled = false;
    }

    public void enableBackButton() {
        backButtonEnabled = true;
    }

    public Spinner getDurationSpinner() {
        return durationSpinner;
    }


    public void overdriveImplementation() {
        //MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        if (!mIsLocked) {
            // First click
            int delayDuration = Integer.parseInt(durationSpinner.getSelectedItem().toString().replaceAll("[^\\d.]", "")) * 60000;

            // Update a TextView to show the time remaining
            // (optional)
            // Execute first action when timer is done
            disable.setText("Cancel");
            boolean active = devicePolicyManager.isAdminActive(componentName);
            if (true) {
                //devicePolicyManager.lockNow();
                //disableBackButton();
                ((MainActivity) requireActivity()).startLock();
                //startLockTask();
                mIsLocked = true;

            }

            // Create a new timer for 5 seconds (adjust as desired)
            timer = new CountDownTimer(delayDuration, 1000) {
                public void onTick(long millisUntilFinished) {
                    // Update a TextView to show the time remaining
                    // (optional)
                    long remainingTime = millisUntilFinished / 1000;
                    remainingTimeTextView.setText("Time Left: " + remainingTime);
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
        //MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        if (!mIsLocked) {
            // First click
            int workDuration = Integer.parseInt(durationSpinner.getSelectedItem().toString().replaceAll("[^\\d.]", "")) * 60000;
            int breakDuration = 5000; // Set your desired break duration in milliseconds

            // Lock the app (blocking)
            lockApp();

            // Create a new timer for the work period
            timer = new CountDownTimer(workDuration, 1000) {
                public void onTick(long millisUntilFinished) {
                    long remainingTime = millisUntilFinished / 1000;
                    remainingTimeTextView.setText("Work Time Left: " + remainingTime);
                }

                public void onFinish() {
                    // Unlock the app (unblocking)
                    unlockApp();

                    // Create a new timer for the break period
                    timer = new CountDownTimer(breakDuration, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long remainingTime = millisUntilFinished / 1000;
                            remainingTimeTextView.setText("Break Time Left: " + remainingTime);
                        }

                        public void onFinish() {
                            // Re-lock the app (blocking) and restart the work timer
                            lockApp();
                            timer.start();
                        }
                    };
                    timer.start(); // Start the break timer
                }
            };
            timer.start(); // Start the work timer
        } else {
            // Second click
            unlockApp();
            timer.cancel();
            remainingTimeTextView.setText("0");
        }
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