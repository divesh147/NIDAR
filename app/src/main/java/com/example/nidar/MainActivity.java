package com.example.nidar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button btnUpdateDetails, btnSpeechRecognition, btnLowBattery, btnFallDetection;
    BatteryLevelReceiver br;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    boolean flag = false;
    Hashtable<String, Integer> permissionCheck;
    private static boolean voiceBtn, batteryBtn, fallBtn;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("NIDAR", MODE_PRIVATE);

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Hello " + pref.getString("name", "") + "!");

        mainScreenDecider();
        requestPermissions();
        enableLocation();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_help_feedback) {
            Toast.makeText(this, "Help and Feedback", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, HelpFeedback.class));
            return true;
        }

        if (id == R.id.about) {
            Toast.makeText(this, "Help and Feedback", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, AboutUsActivity.class));

            return true;
        }

        if (id == R.id.guide) {
            Toast.makeText(this, "Guide", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, Guide.class));
            return true;
        }

        if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
            resetData();
            Toast.makeText(MainActivity.this,"User Signed Out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Initialise Different View Items
    private void findViews() {
        btnUpdateDetails = findViewById(R.id.btn_update_details);
        btnSpeechRecognition = findViewById(R.id.btn_speech_recognition);
        btnLowBattery = findViewById(R.id.btn_low_battery_message);
        btnFallDetection = findViewById(R.id.btn_fall_detection);
    }


    // Set background color and text of views
    private void setViews() {
        voiceBtn = pref.getBoolean("isSpeechOn", false);
        if (!voiceBtn) {
            btnSpeechRecognition.setText(R.string.speech_on);
            btnSpeechRecognition.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        else {
            btnSpeechRecognition.setText(R.string.speech_off);
            btnSpeechRecognition.setBackgroundColor(Color.RED);
        }

        batteryBtn = pref.getBoolean("isBatteryLowOn", false);
        if (!batteryBtn) {
            btnLowBattery.setText(R.string.low_battery_on);
            btnLowBattery.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        else {
            btnLowBattery.setText(R.string.low_battery_off);
            btnLowBattery.setBackgroundColor(Color.RED);
        }

        fallBtn = pref.getBoolean("isFallOn", false);
        if (!fallBtn) {
            btnFallDetection.setText(R.string.fall_detection_on);
            btnFallDetection.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        else {
            btnFallDetection.setText(R.string.fall_detection_off);
            btnFallDetection.setBackgroundColor(Color.RED);
        }
    }


    // Sets all buttons onClickListener
    private void setAllButtons() {
        btnUpdateDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignedIn.class));
                finish();
            }
        });

        btnSpeechRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpeechService.class);
                voiceBtn = pref.getBoolean("isSpeechOn", false);
                if (voiceBtn) {
                    voiceBtn = false;
                    editor = pref.edit();
                    editor.putBoolean("isSpeechOn", voiceBtn);
                    editor.commit();
                    btnSpeechRecognition.setText(R.string.speech_on);
                    btnSpeechRecognition.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    stopService(intent);
                }
                else {
                    voiceBtn = true;
                    editor = pref.edit();
                    editor.putBoolean("isSpeechOn", voiceBtn);
                    editor.commit();
                    btnSpeechRecognition.setText(R.string.speech_off);
                    btnSpeechRecognition.setBackgroundColor(Color.RED);
                    startService(intent);
                }
            }
        });

        btnLowBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BatteryService.class);
                batteryBtn = pref.getBoolean("isBatteryLowOn", false);
                if (batteryBtn) {
                    batteryBtn = false;
                    editor = pref.edit();
                    editor.putBoolean("isBatteryLowOn", batteryBtn);
                    editor.commit();
                    btnLowBattery.setText(R.string.low_battery_on);
                    btnLowBattery.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    stopService(intent);
                }
                else {
                    batteryBtn = true;
                    editor = pref.edit();
                    editor.putBoolean("isBatteryLowOn", batteryBtn);
                    editor.commit();
                    btnLowBattery.setText(R.string.low_battery_off);
                    btnLowBattery.setBackgroundColor(Color.RED);
                    br = new BatteryLevelReceiver();
                    startService(intent);
                }
            }
        });

        btnFallDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FallService.class);
                fallBtn = pref.getBoolean("isFallOn", false);
                if (fallBtn) {
                    fallBtn = false;
                    editor = pref.edit();
                    editor.putBoolean("isFallOn", fallBtn);
                    editor.commit();
                    btnFallDetection.setText(R.string.fall_detection_on);
                    btnFallDetection.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    stopService(intent);
                }
                else {
                    fallBtn = true;
                    editor = pref.edit();
                    editor.putBoolean("isFallOn", fallBtn);
                    editor.commit();
                    btnFallDetection.setText(R.string.fall_detection_off);
                    btnFallDetection.setBackgroundColor(Color.RED);
                    startService(intent);
                }
            }
        });
    }


    // Reset all saved data for initial Activity Deciding once again
    private void resetData() {
        editor = pref.edit();
        editor.clear();
        editor.commit();
    }


    // Decide which Activity to open in the beginning
    private void mainScreenDecider() {

        // If user is signed in and has added his/her contact details
        if (pref.getBoolean("isSignedIn", false) && pref.getBoolean("isDetailsSaved", false)) {
            findViews();
            setViews();
            setAllButtons();
        }

        // If user is signed in but not added his/her contact details
        else if (pref.getBoolean("isSignedIn", false)) {
            startActivity(new Intent(MainActivity.this, SignedIn.class));
            finish();
        }

        // If new user is signing up
        else {
            startActivity(new Intent(MainActivity.this, SignupUser.class));
            finish();
        }
    }


    private void requestPermissions() {
        ArrayList<String> remainingPermission = new ArrayList<String>();

        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
                remainingPermission.add(permission);
        }

        if (!remainingPermission.isEmpty()) {
            ActivityCompat.requestPermissions(this, remainingPermission.toArray(new String[remainingPermission.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }


    private void enableLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionCheck = new Hashtable<String, Integer>();
        for (String s : permissions) {
            permissionCheck.put(s, 0);
        }
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS && grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionCheck.put(permissions[i], 1);
                }
            }
        }
        for (String permission: permissionCheck.keySet()) {
            if (permissionCheck.get(permission) != 1){
                flag = true;
                break;
            }
        }

        if(flag)
            requestPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("In Main Activity", "Here");
    }
}
