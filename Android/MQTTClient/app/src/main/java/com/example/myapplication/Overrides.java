package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

public class Overrides extends AppCompatActivity {
        private boolean backButtonEnabled = true;

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

}
