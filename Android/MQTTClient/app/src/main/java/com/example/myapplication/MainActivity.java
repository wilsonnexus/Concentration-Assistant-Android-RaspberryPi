package com.example.myapplication;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.Menu;

public class MainActivity extends AppCompatActivity {
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;


    // Allowlist one app
    private static final String KIOSK_PACKAGE = "com.example.myapplication";
    private static final String[] APP_PACKAGES = {KIOSK_PACKAGE};
    private boolean backButtonEnabled = true;

    private boolean overdrive = true;
    private boolean mIsLocked = false;
    // Fix phone back button going to Settings in the Home fragment
    // Create current frame indicators
    private enum CurrentFragment {
        HOME, SETTINGS
    }
    private CurrentFragment currentFragment = CurrentFragment.HOME; // Set the initial fragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    }

    @Override
    public void onBackPressed() {
        // Fix phone back button going to Settings in the Home fragment
        if (currentFragment == CurrentFragment.SETTINGS) {
            // If on the settings fragment, navigate back to the home fragment
            if (backButtonEnabled) {
                // If on the home fragment, perform default back navigation
                super.onBackPressed();
            }
        }
    }


    // Fix phone back button going to Settings in the Home fragment
    // We are in the Settings fragment
    public void setCurrentFragmentSettings() {
        currentFragment = CurrentFragment.SETTINGS;
    }
    // We are in the Home fragment
    public void setCurrentFragmentHome() {
        currentFragment = CurrentFragment.HOME;
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
    

    public void startLock() {
        startLockTask();
    }
    public void stopLock() {
        stopLockTask();
    }

    public boolean getOverdrive() {
        return overdrive;
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

}