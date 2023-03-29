package com.example.myapplication;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import android.os.Parcel;
import android.widget.Button;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;



public class FirstFragment extends Fragment{

    private FragmentFirstBinding binding;
    // Allowlist two apps.
    private static final String KIOSK_PACKAGE = "com.example.myapplication";
    //private static final String PLAYER_PACKAGE = "com.example.player";
    private static final String[] APP_PACKAGES = {KIOSK_PACKAGE};
    // ...
    static final int RESULT_ENABLE = 1;
    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;
    Button btn_unblock;
    Button btn_block;
    Context context;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState



    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);



        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });




       /*binding.buttonUnblock.setOnClickListener(new View.OnClickListener() {
        //btn_unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hhh.disableBackButton();


            }
        });*/


        /*binding.buttonUnblock.setOnClickListener(new View.OnClickListener() {
            //btn_unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {*/
                //dpm.setLockTaskPackages(adminName, );
                /*boolean active=devicePolicyManager.isAdminActive(componentName);
                if(active){
                    devicePolicyManager.removeActiveAdmin(componentName);
                    btn_unblock.setText("Enable");
                    btn_block.setVisibility(View.GONE);
                }else{
                    Intent intent=new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,componentName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"You Should Enable the app!");

                    //   startActivityForResult(intent,RESULT_ENABLE);
                    Log.i("Helllooooo", "Not working");
                    activityResultLaunch.launch(intent);

                }*/
            /*}
        });*/




        /*binding.buttonBlock.setOnClickListener(new View.OnClickListener() {
            //btn_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //devicePolicyManager.lockNow();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Set an option to turn on lock task mode when starting the activity.*/
                    /*ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLockTaskEnabled(true);

                    // Start our kiosk app's main activity with our lock task mode option.
                    PackageManager packageManager = context.getPackageManager();
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(KIOSK_PACKAGE);
                    if (launchIntent != null) {
                        context.startActivity(launchIntent, options.toBundle());
                    }
                }
                else {
                        // First, confirm that this package is allowlisted to run in lock task mode.
                        if (devicePolicyManager.isLockTaskPermitted(context.getPackageName())) {
                            getActivity().startLockTask();
                        } else {
                            // Because the package isn't allowlisted, calling startLockTask() here
                            // would put the activity into screen pinning mode.
                        }
                }
            }
        });*/

/*        binding.buttonBlock.setOnClickListener(new View.OnClickListener() {
            //btn_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                devicePolicyManager.lockNow();
*/
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Set an option to turn on lock task mode when starting the activity.
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLockTaskEnabled(true);

                    // Start our kiosk app's main activity with our lock task mode option.
                    PackageManager packageManager = context.getPackageManager();
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(KIOSK_PACKAGE);
                    if (launchIntent != null) {
                        context.startActivity(launchIntent, options.toBundle());
                    }
                }
                else {
                        // First, confirm that this package is allowlisted to run in lock task mode.
                        if (devicePolicyManager.isLockTaskPermitted(context.getPackageName())) {
                            getActivity().startLockTask();
                        } else {
                            // Because the package isn't allowlisted, calling startLockTask() here
                            // would put the activity into screen pinning mode.
                        }
                }*/
/*            }
        });

*/

    }




    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // ToDo : Do your stuff...
                        btn_unblock.setText("Dissable");
                        btn_block.setVisibility(View.VISIBLE);
                        Toast.makeText(context, "Dissabled", Toast.LENGTH_SHORT).show();
                    } else {
                        // ToDo : Do your stuff...
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();

                    }
                }
            });

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case RESULT_ENABLE:
                if(resultCode== Activity.RESULT_OK ){
                    btn_unblock.setText("Dissable");
                    btn_block.setVisibility(View.VISIBLE);
                    Toast.makeText(context, "Dissabled", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                }
                return;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}