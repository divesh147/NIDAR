package com.example.nidar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryLevelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            Toast.makeText(context, "LOW BATTERY", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(context, OwnDialog.class);
            intent1.putExtra("Title", "Battery Low! Do we send an alert?");
            context.startActivity(intent1);
        }
    }
}