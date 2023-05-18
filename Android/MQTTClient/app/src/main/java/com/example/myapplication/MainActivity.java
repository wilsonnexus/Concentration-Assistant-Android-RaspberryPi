package com.example.myapplication;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import com.example.myapplication.helper.MqttHelper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/*
* Author: Wilson Neira
* Description: This app is used along a Raspberry Pi vibration motor
* and EEG to alert a user when they have stopped concentrating on a task.
* */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        View firstfragmentContent = findViewById(R.id.fragment_first);
        firstfragmentContent.setVisibility(View.GONE);

        //setContentView(R.layout.activity_main);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        componentName = new ComponentName(this, Controller.class);

        disable = (Button) findViewById(R.id.disable);
        remainingTimeTextView = findViewById(R.id.textView);
        durationSpinner = findViewById(R.id.durationSpinner);

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item, durationsList);

        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        durationSpinner.setAdapter(adapter);


        disable.setOnClickListener(this);

        dataReceived = (TextView) findViewById(R.id.textview_first);

        startMqtt();

    }


    @Override
    public void onBackPressed() {
        if (backButtonEnabled) {

            super.onBackPressed();
        }
    }

    public void disableBackButton() {
        backButtonEnabled = false;
    }

    public void enableBackButton() {
        backButtonEnabled = true;
    }




    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(componentName);
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

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIsLocked) {
            startLockTask();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Find the main activity content and hide it
            View mainActivityContent = findViewById(R.id.main_activity_content);
            mainActivityContent.setVisibility(View.GONE);

            // Navigate to the FirstFragment (SettingsFragment) when the settings button is clicked
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.FirstFragment);
            //settingsClicked = true; // set settingsClicked to true
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void openSettings(View view) {

        // Find the main activity content and hide it
        View mainActivityContent = findViewById(R.id.main_activity_content);
        mainActivityContent.setVisibility(View.GONE);

        // Navigate to the FirstFragment (SettingsFragment) when the settings button is clicked
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.FirstFragment);
        toggleNavHostFragment(true);
    }



   public void toggleNavHostFragment(boolean show) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment);

        if (show) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(navHostFragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(navHostFragment)
                    .commit();
        }
    }

    public void showMainContent() {
        // Find the main activity content and show it
        View mainActivityContent = findViewById(R.id.main_activity_content);
        mainActivityContent.setVisibility(View.VISIBLE);

        // Hide the NavHostFragment
        toggleNavHostFragment(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    // Modes
    public void startPomodoroTimer() {
        // Implement the Pomodoro timer logic
        // For example, you can use the existing timer logic with some modifications
        overdrive = false;
    }

    public void startOverdriveTimer() {
        // Implement the Overdrive timer logic
        // For example, you can use the existing timer logic without breaks
        overdrive = true;

    }

    public void overdriveImplementation() {
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
                startLockTask();
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
                    stopLockTask();
                    mIsLocked = false;
                    remainingTimeTextView.setText("");
                }
            };
            timer.start(); // Start the new timer
        } else {


            // Second click
            disable.setText("Disable");
            timer.cancel();
            stopLockTask();
            mIsLocked = false;
            remainingTimeTextView.setText("0");
            // Turn vibration motor on
            mqttHelper.publishToTopic("sensor/temp", "1");

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
            startLockTask();
            mIsLocked = true;
        }
    }

    private void unlockApp() {
        disable.setText("Disable");
        stopLockTask();
        mIsLocked = false;
        remainingTimeTextView.setText("");
        // Turn vibration motor on
        mqttHelper.publishToTopic("sensor/temp", "1");

        // Turn vibration motor off
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mqttHelper.publishToTopic("sensor/temp", "0");
            }
        }, 5000);
    }




    // Start MQTT Client Code
    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
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