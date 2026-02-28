package com.example.noipupdater;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LogsActivity extends AppCompatActivity {
    
    private ScrollView scrollView;
    private LinearLayout logsContainer;
    private Button clearButton;
    
    private SharedPreferences logsPrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.logs_title);
        }
        
        logsPrefs = getSharedPreferences("NoIpLogs", MODE_PRIVATE);
        
        initViews();
        setupListeners();
        loadLogs();
    }
    
    private void initViews() {
        scrollView = findViewById(R.id.logsScrollView);
        logsContainer = findViewById(R.id.logsContainer);
        clearButton = findViewById(R.id.clearLogsButton);
    }
    
    private void setupListeners() {
        clearButton.setOnClickListener(v -> {
            logsPrefs.edit().putString("logs", "").apply();
            loadLogs();
        });
    }
    
    private void loadLogs() {
        logsContainer.removeAllViews();
        
        String logs = logsPrefs.getString("logs", "");
        
        if (logs.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText(R.string.no_logs);
            emptyText.setTextSize(16);
            emptyText.setTextColor(Color.GRAY);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(32, 64, 32, 64);
            logsContainer.addView(emptyText);
            return;
        }
        
        String[] logLines = logs.split("\n");
        for (String line : logLines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length < 3) continue;
            
            String timestamp = parts[0];
            String type = parts[1];
            String message = parts[2];
            
            LinearLayout logItem = createLogItem(timestamp, type, message);
            logsContainer.addView(logItem);
        }
    }
    
    private LinearLayout createLogItem(String timestamp, String type, String message) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 16, 24, 16);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        container.setLayoutParams(params);
        
        // Háttérszín típus szerint
        int backgroundColor;
        int borderColor;
        switch (type) {
            case "success":
                backgroundColor = Color.parseColor("#E8F5E9");
                borderColor = Color.parseColor("#4CAF50");
                break;
            case "error":
                backgroundColor = Color.parseColor("#FFEBEE");
                borderColor = Color.parseColor("#F44336");
                break;
            case "warning":
                backgroundColor = Color.parseColor("#FFF9C4");
                borderColor = Color.parseColor("#FFC107");
                break;
            default:
                backgroundColor = Color.parseColor("#E3F2FD");
                borderColor = Color.parseColor("#2196F3");
        }
        
        container.setBackgroundColor(backgroundColor);
        
        // Bal oldali border szimuláció
        View border = new View(this);
        LinearLayout.LayoutParams borderParams = new LinearLayout.LayoutParams(8, 
            LinearLayout.LayoutParams.MATCH_PARENT);
        border.setLayoutParams(borderParams);
        border.setBackgroundColor(borderColor);
        
        // Időbélyeg
        TextView timestampView = new TextView(this);
        timestampView.setText(timestamp);
        timestampView.setTextSize(12);
        timestampView.setTextColor(Color.GRAY);
        timestampView.setPadding(0, 0, 0, 8);
        
        // Üzenet
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(14);
        messageView.setTextColor(Color.BLACK);
        
        container.addView(timestampView);
        container.addView(messageView);
        
        return container;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}