package com.example.noipupdater;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateService extends Service {
    
    public static final String ACTION_MANUAL_UPDATE = "com.example.noipupdater.MANUAL_UPDATE";
    private static final String TAG = "UpdateService";
    
    private SharedPreferences prefs;
    private Handler handler;
    
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("NoIpPrefs", MODE_PRIVATE);
        handler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_MANUAL_UPDATE.equals(intent.getAction())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    performUpdate();
                }
            }).start();
        }
        return START_NOT_STICKY;
    }
    
    private void performUpdate() {
        // Ellenőrizzük hogy WiFi-n vagyunk-e
        if (!isConnectedToWifi()) {
            addLog("Figyelmeztetés: Nem WiFi kapcsolat", "warning");
            return;
        }
        
        try {
            // Külső IP cím lekérése
            String externalIp = getExternalIp();
            if (externalIp == null) {
                addLog("Hiba: Nem sikerült lekérni a külső IP címet", "error");
                return;
            }
            
            // IP cím mentése
            prefs.edit().putString("current_ip", externalIp).apply();
            
            // No-IP frissítés
            String result = updateNoIp(externalIp);
            
            // Időbélyeg mentése
            String timestamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", 
                new Locale("hu", "HU")).format(new Date());
            prefs.edit().putString("last_update", timestamp).apply();
            
            // Eredmény kiértékelése
            if (result.contains("good") || result.contains("nochg")) {
                addLog("Sikeres frissítés (" + externalIp + "): " + result, "success");
            } else {
                addLog("Frissítés válasz (" + externalIp + "): " + result, "warning");
            }
            
        } catch (Exception e) {
            addLog("Hiba történt: " + e.getMessage(), "error");
            Log.e(TAG, "Update error", e);
        }
    }
    
    private boolean isConnectedToWifi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && 
               activeNetwork.getType() == ConnectivityManager.TYPE_WIFI &&
               activeNetwork.isConnected();
    }
    
    private String getExternalIp() {
        String[] services = {
            "https://api.ipify.org",
            "https://icanhazip.com",
            "https://ifconfig.me/ip",
            "https://ipinfo.io/ip"
        };
        
        for (String serviceUrl : services) {
            try {
                URL url = new URL(serviceUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                    );
                    String ip = reader.readLine().trim();
                    reader.close();
                    conn.disconnect();
                    
                    if (ip != null && ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                        return ip;
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Failed to get IP from " + serviceUrl, e);
            }
        }
        return null;
    }
    
    private String updateNoIp(String ip) throws Exception {
        String hostname = prefs.getString("hostname", "panoramaapartment.sytes.net");
        String email = prefs.getString("email", "gerzsony@yahoo.com");
        String password = prefs.getString("password", "");
        String apiUrl = prefs.getString("api_url", "http://dynupdate.no-ip.com/nic/update");
        
        String urlString = apiUrl + "?hostname=" + hostname + "&myip=" + ip;
        URL url = new URL(urlString);
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        // Basic Authentication
        String credentials = email + ":" + password;
        String encodedCredentials = Base64.encodeToString(
            credentials.getBytes(), 
            Base64.NO_WRAP
        );
        conn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        conn.setRequestProperty("User-Agent", "No-IP Android App/1.0");
        
        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        String response = reader.readLine();
        reader.close();
        conn.disconnect();
        
        return response != null ? response : "No response";
    }
    
    private void addLog(String message, String type) {
        String timestamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", 
            new Locale("hu", "HU")).format(new Date());
        
        SharedPreferences logsPrefs = getSharedPreferences("NoIpLogs", MODE_PRIVATE);
        String logs = logsPrefs.getString("logs", "");
        
        String newLog = timestamp + "|" + type + "|" + message + "\n";
        logs = newLog + logs;
        
        // Korlátozás: max 100 sor
        String[] lines = logs.split("\n");
        if (lines.length > 100) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                sb.append(lines[i]).append("\n");
            }
            logs = sb.toString();
        }
        
        logsPrefs.edit().putString("logs", logs).apply();
        Log.d(TAG, message);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}