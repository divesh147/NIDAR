package com.example.nidar;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Guide extends AppCompatActivity {
    WebView overviewGuide, speechRecognitionGuide, fallDetectionGuide, batteryManagerGuide, activationProcessGuide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        overviewGuide = findViewById(R.id.overview_guide);
        overviewGuide.setVerticalScrollBarEnabled(false);
        overviewGuide.loadData(getString(R.string.overview_guide), "text/html; charset=utf-8", "utf-8");

        speechRecognitionGuide = findViewById(R.id.speech_recognition_guide);
        speechRecognitionGuide.setVerticalScrollBarEnabled(false);
        speechRecognitionGuide.loadData(getString(R.string.speech_recognition_guide), "text/html; charset=utf-8", "utf-8");

        fallDetectionGuide = findViewById(R.id.fall_detection_guide);
        fallDetectionGuide.setVerticalScrollBarEnabled(false);
        fallDetectionGuide.loadData(getString(R.string.fall_detection_guide), "text/html; charset=utf-8", "utf-8");

        batteryManagerGuide = findViewById(R.id.battery_manager_guide);
        batteryManagerGuide.setVerticalScrollBarEnabled(false);
        batteryManagerGuide.loadData(getString(R.string.battery_manager_guide), "text/html; charset=utf-8", "utf-8");

        activationProcessGuide = findViewById(R.id.activation_process_guide);
        activationProcessGuide.setVerticalScrollBarEnabled(false);
        activationProcessGuide.loadData(getString(R.string.activation_process_guide), "text/html; charset=utf-8", "utf-8");
    }
}

