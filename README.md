# No-IP DDNS Updater Android App

## Project structure

```
NoIpUpdater/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/noipupdater/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SettingsActivity.java
│   │   │   │   ├── LogsActivity.java
│   │   │   │   ├── UpdateService.java
│   │   │   │   └── UpdateReceiver.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_settings.xml
│   │   │   │   │   └── activity_logs.xml
│   │   │   │   └── values/
│   │   │   │       └── strings.xml
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle (projekt szintű)
```

## Functions

### Main Page
- **Start/Stop Button**: Starts or stops automatic IP update
- **Status Display**: Shows current status, IP address and last update time
- **Instant Update**: Updates IP address immediately (only when active)

### Settings
- **Hostname**: The No-IP hostname (e.g. panoramaapartment.sytes.net)
- **Email**: No-IP account email address
- **Password**: No-IP account password
- **API URL**: No-IP API endpoint
- **Date format**: Timestamp format in logs

### Logs
- Lists all update events in chronological order
- Color-coded messages (green=success, red=error, yellow=warning)
- Delete function

## Technical Details

### Running in the background
- **AlarmManager**: Automatically updates every hour
- **BroadcastReceiver**: Receives alarm events
- **Service**: Performs IP update

### WiFi Check
The app will only update over WiFi to ensure it reports the IP address of the local router:
```java
ConnectivityManager.TYPE_WIFI
```

### Get IP address
Tries multiple services in a row:
- api.ipify.org
- icanhazip.com
- ifconfig.me/ip
- ipinfo.io/ip

### Data storage
- **SharedPreferences** for settings
- **SharedPreferences** for logs
- No database, simple key-value storage

## Permissions

The app uses the following permissions:
- `INTERNET`: Get IP and call No-IP API
- `ACCESS_NETWORK_STATE`: Check network connection
- `ACCESS_WIFI_STATE`: Check WiFi connection
- `SCHEDULE_EXACT_ALARM`: Exact timing for hourly updates
- `RECEIVE_BOOT_COMPLETED`: Resume after reboot
- `WAKE_LOCK`: Wake device on update

