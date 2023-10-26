package com.example.myapplication;
import android.app.admin.DevicePolicyManager;
import android.os.Vibrator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import androidx.test.platform.app.InstrumentationRegistry;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class ExampleUnitTest {
    @Mock
    private DevicePolicyManager devicePolicyManager;
    @Mock
    private Vibrator vibrator;

    private MainFragment mainFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); // Initialize mocks

        // Create your MainFragment and inject the mocked dependencies
        mainFragment = new MainFragment(devicePolicyManager, null, vibrator);
        // Optionally, you can set up any other dependencies or context here.
    }

    /*@Test
    public void defaultSpinnerSelection_shouldReturnCorrectValue() {
        Spinner spinner = mainFragment.getDurationSpinner();
        assertNotNull(spinner);

        // Set a default selection
        String[] durations = mainFragment.getResources().getStringArray(R.array.durations_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainFragment.requireContext(), R.layout.custom_spinner_item, durations);
        spinner.setAdapter(adapter);
        spinner.setSelection(1);

        String selection = spinner.getSelectedItem().toString();
        assertEquals("1 minute", selection);
    }*/

    @Test
    public void vibratorIsNotNull() {
        Vibrator vibrator = mainFragment.getVibrator();
        assertNotNull(vibrator);
    }

    @Test
    public void devicePolicyManagerIsNotNull() {
        DevicePolicyManager devicePolicyManager = mainFragment.getDevicePolicyManager();
        assertNotNull(devicePolicyManager);
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }





}