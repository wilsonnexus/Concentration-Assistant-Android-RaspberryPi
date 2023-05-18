package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import android.widget.Button;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Button pomodoroButton;
    private Button overdriveButton;
    //private Button buttonFirst;
    //private Button buttonGoBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        //buttonFirst = binding.buttonFirst;

        //buttonFirst.setVisibility(View.GONE); // hide the button initially
        //buttonFirst = (Button) findViewById(R.id.button_first);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        // This button enables you to go back to MainActivity
        binding.goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showMainContent();
                }
            }
        });

        // This button enables you to set the timer to the Pomodoro Technique Mode
        pomodoroButton = binding.pomodoroButton;
        pomodoroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).startPomodoroTimer();
                }
            }
        });

        // This button enables you to set the timer to the Overdrive Mode
        overdriveButton = binding.overdriveButton;
        overdriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).startOverdriveTimer();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            // Show the button only when the title of the action bar is "Settings"

        }
    }

    /*public Button getButtonFirst() {
        return buttonFirst;
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
