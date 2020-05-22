package com.example.nidar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class OwnDialog extends AppCompatActivity {

    private final String LOG_TAG = "OwnDialog Activity";
    private Timer t;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;


    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.own_dialog);
        setFinishOnTouchOutside(false);
        setTurnScreenOn(true);
        setShowWhenLocked(true);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 8, 0);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenAwake = (Build.VERSION.SDK_INT < 20 ? powerManager.isScreenOn() : powerManager.isInteractive());
        if (!isScreenAwake) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wakeLock.acquire(3000); //set your time in milliseconds
        }
        createAlertDialog();
    }


    @Override
    public void onBackPressed() {
        //Doing Nothing So that the dialog does not disappear on pressing the back button.
    }


    // Puts up an Alert Dialog Box
    private void createAlertDialog() {
        String title = getIntent().getStringExtra("Title");
        TextView textView = findViewById(R.id.tv_problem);
        textView.setText(title);

        mediaPlayer = MediaPlayer.create(this, R.raw.sos);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        Button no = findViewById(R.id.No);
        Button yes = findViewById(R.id.Yes);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.cancel();
                callbackCallingClass();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.cancel();
                sendMessage();
                callbackCallingClass();
            }
        });

        t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                Log.i(LOG_TAG, "In Timer");
                sendMessage();
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                callbackCallingClass();
            }
        }, 15000); // after 15 second (or 15000 miliseconds), the task will be active.
    }


    // Return control to the class which called OwnDialog
    private void callbackCallingClass() {
        mediaPlayer.stop();
        String callingClass = getIntent().getStringExtra("Calling Class");
        if (callingClass.equals("Speech Service")) {
            Intent intent = new Intent(OwnDialog.this, SpeechService.class);
            startService(intent);
        }
        else if (callingClass.equals("Fall Service")) {
            Intent intent = new Intent(OwnDialog.this, FallService.class);
            startService(intent);
        }
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        finish();
    }


    // Composing and send a Message with person's location
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

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double latitude, longitude;

        if (location!=null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            String message = (getIntent().getStringExtra("Calling Class").equals("Battery Level Receiver") ? "My battery is low." : "I am in danger.");
            message += " My current location is : https://maps.google.com/maps?q=" + latitude + "," + longitude;
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
                    // Send message to all saved contacts
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
