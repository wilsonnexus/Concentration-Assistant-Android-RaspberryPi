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


/*
* Author: Wilson Neira
* Desciprion: This app is used along a Raspberry Pi vibration motor
* and EEG to alert a user when they have stopped concentrating on a task.
* */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button lock, disable, enable;
    private static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName componentName;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    MqttHelper mqttHelper;

    TextView dataReceived;
    private Button btnPublish;


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

        //setContentView(R.layout.activity_main);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        componentName = new ComponentName(this, Controller.class);



        //lock = (Button) findViewById(R.id.lock);
        //enable = (Button) findViewById(R.id.enable);
        disable = (Button) findViewById(R.id.disable);
        remainingTimeTextView = findViewById(R.id.textView);
        durationSpinner = findViewById(R.id.durationSpinner);
        //Log.d("TAGGER", R.array.durations_array);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.durations_array, android.R.layout.simple_spinner_item);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        //lock.setOnClickListener(this);
        //enable.setOnClickListener(this);
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
        /*if (view == lock) {
            boolean active = devicePolicyManager.isAdminActive(componentName);
            if(true) {
                //devicePolicyManager.lockNow();
                //disableBackButton();
                startLockTask();
                mIsLocked = true;
                // Disable the "screen pinned mode" message
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                 //   devicePolicyManager.setLockTaskFeatures(componentName, DevicePolicyManager.LOCK_TASK_FEATURE_HOME);
                //}
            } else {
                Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
            }
        } else if (view == enable) {
            stopLockTask();
            mIsLocked = false;


        }*/ if (view == disable) {
                if (!mIsLocked) {
                    // First click
                    int delayDuration = Integer.parseInt(durationSpinner.getSelectedItem().toString()) * 60000;

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

                    } //else {
                    //Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
                    //}

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
                    /*try {
                        // Wait for 5 seconds
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
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
               /* }
            };
            timer.start(); // Start the initial */
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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