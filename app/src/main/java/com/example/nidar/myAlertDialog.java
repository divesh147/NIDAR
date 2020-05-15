package com.example.nidar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class myAlertDialog extends AppCompatActivity {

    private Timer t;
    private LocationManager locationManager;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setFinishOnTouchOutside(false);

        builder = new AlertDialog.Builder(this);
        //final Intent intent = new Intent(this, SpeechService.class);

        builder.setTitle("Are you in a problem?")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        t.cancel();
                        Toast.makeText(myAlertDialog.this, "NO", Toast.LENGTH_LONG).show();
                        /*startService(intent);
                        finish();*/
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(myAlertDialog.this, "YES", Toast.LENGTH_LONG).show();
                        /*t.cancel();
                        sendMessage();
                        startService(intent);
                        finish();*/
                    }
                });

        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();

        t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                //sendMessage();
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                //startService(intent);
                //finish();
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
        if (location != null) {
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
                        String contactPerson = dataSnapshot.child(String.valueOf(i)).getValue().toString();
                        if (contactPerson.matches("[0-9]{10}"))
                            android.telephony.SmsManager.getDefault().sendTextMessage(contactPerson, null, sendMessage, null, null);
                    }
                    Toast.makeText(myAlertDialog.this, "Message Sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(myAlertDialog.this, "DATABASE ERROR", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
