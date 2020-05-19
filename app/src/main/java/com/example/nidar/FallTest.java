package com.example.nidar;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FallTest extends Activity implements SensorEventListener {

    private static final String LOG_TAG = "In FallTest";
    private SensorManager sensorManager;
    private Sensor sensor;
    CountDownTimer timer;
    double sum;
    int run_count = 0;
    double low_long_lie = 9.6, high_long_lie = 10.1;
    boolean personUnconscious;
    ArrayList<Double> acc_window;
    ArrayList<Long> time_window;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Checking Fall", Toast.LENGTH_LONG).show();
        personUnconscious = false;
        acc_window = new ArrayList<>();
        time_window = new ArrayList<>();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    // TODO DELETE THIS LOG MESS
    public void appendLog(String text) {
        Log.i(LOG_TAG, "appendLog");
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists()) {
            try {
                Log.i(LOG_TAG, "FileCreated");
                logFile.createNewFile();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            Log.i(LOG_TAG, "dataWritten");
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void onSensorChanged(final SensorEvent event) {
        appendLog( " In FallTest ");
        run_count++;
        if (run_count == 1) {
            timer = new CountDownTimer(5000, 100) {

                public void onTick(long millisUntilFinished) {
                    sum = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
                    time_window.add(System.currentTimeMillis());
                    appendLog("" + sum + " " + System.currentTimeMillis());
                    acc_window.add(sum);
                }

                public void onFinish() {
                    for (int i = 0; i < acc_window.size(); i++) {
                        if ((acc_window.get(i) >= low_long_lie) && (acc_window.get(i) <= high_long_lie)) {
                            // Check if person is unconscious after high impact
                            for (int j = i + 1; j < acc_window.size(); j++) {
                                if ((acc_window.get(j) >= low_long_lie) && (acc_window.get(j) <= high_long_lie)) {
                                    // if person is unconscious for more than 3 seconds
                                    if (time_window.get(j) - time_window.get(i) >= 3000) {
                                        personUnconscious = true;
                                        break;
                                    }
                                }
                                else break;
                            }
                            if (personUnconscious) break;
                        }
                    }

                    if (personUnconscious) {
                        Intent intent = new Intent(FallTest.this, OwnDialog.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.putExtra("Title", "Did you fall?");
                        intent.putExtra("Calling Class", "Fall Service");
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(FallTest.this, FallService.class);
                        startService(intent);
                    }
                    sensorManager.unregisterListener(FallTest.this);
                    finish();
                }
            }.start();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
