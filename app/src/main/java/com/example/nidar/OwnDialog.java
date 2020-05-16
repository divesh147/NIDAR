package com.example.nidar;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class OwnDialog extends AppCompatActivity {

    private Button no, yes;
    private final String LOG_TAG = "OwnDialog Activity";
    private Timer t;
    private LocationManager locationManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.own_dialog);
        setFinishOnTouchOutside(false);
        setTurnScreenOn(true);
        setShowWhenLocked(true);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenAwake = (Build.VERSION.SDK_INT < 20 ? powerManager.isScreenOn() : powerManager.isInteractive());
        if(!isScreenAwake){
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wakeLock.acquire(3000); //set your time in milliseconds
            createAlertDialog();
        }
        else {
            createAlertDialog();
        }

    /*
        AudioManager aud = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (aud.getStreamVolume(AudioManager.STREAM_NOTIFICATION) == 0) {
            Log.i(LOG_TAG, "SHOW");
            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vib.vibrate(5000);
        }
        else {
            ToneGenerator t = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, aud.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
            t.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
        }
         */
    }

    private void createAlertDialog(){

            String serviceName = getIntent().getStringExtra("Title");

            textView = findViewById(R.id.tv_problem);
            textView.setText(serviceName);
            final Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vib.vibrate(200); // To vibrate phone till button is clicked

            no = findViewById(R.id.No);
            yes = findViewById(R.id.Yes);
            final Intent intent = new Intent(OwnDialog.this, SpeechService.class);

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    t.cancel();
                    startService(intent);
                    finish();
                }
            });

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    t.cancel();
                    sendMessage();
                    startService(intent);
                    finish();
                }
            });

            t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    Log.i(LOG_TAG, "In Timer");
                    sendMessage();
                    t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                    startService(intent);
                    finish();
                }
            }, 10000); // after 2 second (or 2000 miliseconds), the task will be active.
        }

    private void sendMessage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double latitude, longitude;

        if (location!=null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            String message = "https://maps.google.com/maps?q=" + latitude + "," + longitude;
            StringBuffer smsBody = new StringBuffer();
            smsBody.append(Uri.parse(message));
            final String sendMessage = smsBody.toString();

            // Fetch user's phone number
            SharedPreferences pref = getSharedPreferences("NIDAR", MODE_PRIVATE);
            String myPhoneNumber = pref.getString("phoneNumber", null);

            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(myPhoneNumber);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (int i = 1; i < 5; i++) {
                        String contactPerson = dataSnapshot.child( String.valueOf(i) ).getValue().toString();
                        if (contactPerson.matches("[0-9]{10}"))
                            android.telephony.SmsManager.getDefault().sendTextMessage(contactPerson, null, sendMessage, null, null);
                    }
                    Toast.makeText(OwnDialog.this, "Message Sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(OwnDialog.this, "DATABASE ERROR", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
