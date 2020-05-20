package com.example.nidar;

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
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FallTest extends Service implements SensorEventListener {

    private static final String LOG_TAG = "In FallTest";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Checking Fall", Toast.LENGTH_LONG).show();
        personUnconscious = false;
        acc_window = new ArrayList<>();
        time_window = new ArrayList<>();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
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
            timer = new CountDownTimer(5500, 100) {

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
                    stopSelf();
                }
            }.start();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
        Intent restartServiceIntent = new Intent(getApplicationContext(), FallService.class);
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(intent);
    }
}
