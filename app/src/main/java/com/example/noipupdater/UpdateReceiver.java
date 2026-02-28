package com.example.noipupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class UpdateReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("NoIpPrefs", Context.MODE_PRIVATE);
        boolean isRunning = prefs.getBoolean("is_running", false);
        
        if (isRunning) {
            Intent serviceIntent = new Intent(context, UpdateService.class);
            serviceIntent.setAction(UpdateService.ACTION_MANUAL_UPDATE);
            context.startService(serviceIntent);
        }
    }
}