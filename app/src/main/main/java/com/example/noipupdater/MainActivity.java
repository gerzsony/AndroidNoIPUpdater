package com.example.noipupdater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private TextView statusText;
    private TextView currentIpText;
    private TextView lastUpdateText;
    private Button toggleButton;
    private Button settingsButton;
    private Button logsButton;
    private Button manualUpdateButton;
    
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "NoIpPrefs";
    private static final String KEY_IS_RUNNING = "is_running";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        setupListeners();
        updateUI();
    }
    
    private void initViews() {
        statusText = findViewById(R.id.statusText);
        currentIpText = findViewById(R.id.currentIpText);
        lastUpdateText = findViewById(R.id.lastUpdateText);
        toggleButton = findViewById(R.id.toggleButton);
        settingsButton = findViewById(R.id.settingsButton);
        logsButton = findViewById(R.id.logsButton);
        manualUpdateButton = findViewById(R.id.manualUpdateButton);
    }
    
    private void setupListeners() {
        toggleButton.setOnClickListener(v -> toggleService());
        
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        logsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogsActivity.class);
            startActivity(intent);
        });
        
        manualUpdateButton.setOnClickListener(v -> {
            if (isServiceRunning()) {
                Intent intent = new Intent(this, UpdateService.class);
                intent.setAction(UpdateService.ACTION_MANUAL_UPDATE);
                startService(intent);
            }
        });
    }
    
    private void toggleService() {
        boolean isRunning = isServiceRunning();
        
        if (isRunning) {
            stopUpdateService();
        } else {
            startUpdateService();
        }
        
        updateUI();
    }
    
    private void startUpdateService() {
        prefs.edit().putBoolean(KEY_IS_RUNNING, true).apply();
        
        // Azonnali frissítés
        Intent intent = new Intent(this, UpdateService.class);
        intent.setAction(UpdateService.ACTION_MANUAL_UPDATE);
        startService(intent);
        
        // Óránkénti ütemezés
        schedulePeriodicUpdate();
    }
    
    private void stopUpdateService() {
        prefs.edit().putBoolean(KEY_IS_RUNNING, false).apply();
        cancelPeriodicUpdate();
    }
    
    private void schedulePeriodicUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        long interval = 60 * 60 * 1000; // 1 óra
        long triggerTime = System.currentTimeMillis() + interval;
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            interval,
            pendingIntent
        );
    }
    
    private void cancelPeriodicUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
    
    private boolean isServiceRunning() {
        return prefs.getBoolean(KEY_IS_RUNNING, false);
    }
    
    private void updateUI() {
        boolean isRunning = isServiceRunning();
        
        if (isRunning) {
            statusText.setText("Státusz: Aktív");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            toggleButton.setText("Leállítás");
            toggleButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            manualUpdateButton.setVisibility(View.VISIBLE);
        } else {
            statusText.setText("Státusz: Inaktív");
            statusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            toggleButton.setText("Indítás");
            toggleButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            manualUpdateButton.setVisibility(View.GONE);
        }
        
        String currentIp = prefs.getString("current_ip", "-");
        currentIpText.setText("Jelenlegi IP: " + currentIp);
        
        String lastUpdate = prefs.getString("last_update", "-");
        lastUpdateText.setText("Utolsó frissítés: " + lastUpdate);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}