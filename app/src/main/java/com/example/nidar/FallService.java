package com.example.nidar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FallService extends Service implements SensorEventListener {

    private static final String LOG_TAG = "Fall Service Activity";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private SensorManager sensorManager;
    private Sensor sensor;
    double sum;
    boolean min, max;
    double T_free_fall = 4.511059, T_shock = 29.41995;
    long free_fall_time, shock_time, currentTime;


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        // On tapping the notification, it will open the application's main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("App is running in background")
                .setContentText("Tap to open the application")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
    }


    // TODO DELETE THIS LOG MESS
    public void appendLog(String text) {
        Log.i(LOG_TAG, "appendLog");
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists()) {
            try {
                Log.i(LOG_TAG, "FileCreated");
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            Log.i(LOG_TAG, "dataWritten");
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sum = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
            currentTime = System.currentTimeMillis();
            appendLog(String.valueOf(sum));
        }

        /*
        1g = 9.80665 m/s^2
        T_free_fall  4.511059 m/s^2 = 0.46g
        T_shock      29.41995 m/s^2 = 3g
        T_duration   800 ms
         */
        if (sum <= T_free_fall) {
            appendLog("Min : " + sum + " Time : " + currentTime);
            min = true;
            free_fall_time = currentTime;
        }

        if (min) {
            if (sum >= T_shock) {
                appendLog("Max : " + sum + " Time : " + currentTime);
                max = true;
                shock_time = currentTime;
            }
        }

        if (max) {
            if (shock_time - free_fall_time <= 800) {
                appendLog("Suspected Fall");
                sensorManager.unregisterListener(this);
                // Check for person unconscious behaviour
                Intent intent = new Intent(FallService.this, FallTest.class);
                startService(intent);
                stopSelf();
            }
            min = max = false;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Fall Service Started", Toast.LENGTH_LONG).show();

        min = max = false;
        // Registering The Sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onTaskRemoved(Intent intent) {
        Log.i(LOG_TAG, "onTaskRemoved");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(intent);
    }
}