package com.example.noipupdater;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    private EditText hostnameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private EditText apiUrlEdit;
    private EditText dateFormatEdit;
    private Button saveButton;
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Vissza gomb az action bar-ban
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        
        prefs = getSharedPreferences("NoIpPrefs", MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        hostnameEdit = findViewById(R.id.hostnameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        apiUrlEdit = findViewById(R.id.apiUrlEdit);
        dateFormatEdit = findViewById(R.id.dateFormatEdit);
        saveButton = findViewById(R.id.saveButton);
    }
    
    private void loadSettings() {
        hostnameEdit.setText(prefs.getString("hostname", "panoramaapartment.sytes.net"));
        emailEdit.setText(prefs.getString("email", "gerzsony@yahoo.com"));
        passwordEdit.setText(prefs.getString("password", ""));
        apiUrlEdit.setText(prefs.getString("api_url", "http://dynupdate.no-ip.com/nic/update"));
        dateFormatEdit.setText(prefs.getString("date_format", "%d-%b-%Y %T"));
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveSettings());
    }
    
    private void saveSettings() {
        String hostname = hostnameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString();
        String apiUrl = apiUrlEdit.getText().toString().trim();
        String dateFormat = dateFormatEdit.getText().toString().trim();
        
        if (hostname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_required_fields, 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("hostname", hostname);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("api_url", apiUrl);
        editor.putString("date_format", dateFormat);
        editor.apply();
        
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
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