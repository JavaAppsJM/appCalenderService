package be.hvwebsites.standup;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // Notification ID.
    private static final int NOTIFICATION_ID = 0;
    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    private NotificationManager mNotificationManager;

    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*
        // Set an alarm with common intent
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm Stand Up")
                    .putExtra(AlarmClock.EXTRA_HOUR, 11)
                    .putExtra(AlarmClock.EXTRA_MINUTES, 35);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
*/

/*
        // Set Calender event with common intent
        long begin = Calendar.getInstance().getTimeInMillis();
        long end = begin + (1000 * 60 * 60 * 24);
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, "Event Stand Up")
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "Event location")
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
*/

        // add calendar event directly
        // query calender ID with projection array declared earlier
        // Run query
        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = new String[] {CalendarContract.ACCOUNT_TYPE_LOCAL};
//        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
//                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
//                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
//        String[] selectionArgs = new String[] {"hera@example.com", "LOCAL",
//                "hera@example.com"};
        // Submit the query and get a Cursor object back.
        String[] permissions = {
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
        == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, permissions, 0);

        }
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        long calID = 0;
        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);// ID off the calendar
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
            int debug = 0;

            // Do something with the values...

        }

        // add an event in the calendar
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2022, 3, 7, 7, 30);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2022, 3, 7, 8, 45);
        endMillis = endTime.getTimeInMillis();

//        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, "Event title");
        values.put(CalendarContract.Events.DESCRIPTION, "Test om iets in agenda te schrijven");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "WesternEurope/Brussels");
        Uri uriEvent = cr.insert(CalendarContract.Events.CONTENT_URI, values);

// get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uriEvent.getLastPathSegment());
        boolean debug = true;
//
// ... do something with event ID
//
//

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // TODO: de alarm manager werkt niet, er wordt geen alarm gescheduled !
        ToggleButton alarmToggle = findViewById(R.id.alarmToggle);
        boolean alarmUp;

        Intent notifyIntent = new Intent(this, AlarmReceiver.class);

        if (PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                notifyIntent,
                PendingIntent.FLAG_NO_CREATE) != null){
            alarmUp = true;
        } else {
            alarmUp = false;
        }
        alarmToggle.setChecked(alarmUp);


        final PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String toastMessage;
                long repeatInterval = 300000;
                long triggerTime = SystemClock.elapsedRealtime()
                        + repeatInterval;

                if(isChecked){
//                    deliverNotification(MainActivity.this);
                    //Set the toast message for the "on" case
                    if (alarmManager != null){
                        alarmManager.setInexactRepeating(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                triggerTime,
                                repeatInterval,
                                notifyPendingIntent);
                    }
                    toastMessage = "Stand Up Alarm On!";
                } else {
                    //Cancel notification if the alarm is turned off
                    if (alarmManager != null){
                        alarmManager.cancel(notifyPendingIntent);
                    }
                    mNotificationManager.cancelAll();

                    //Set the toast message for the "off" case
                    toastMessage = "Stand Up Alarm Off!";
                }

                //Show a toast to say the alarm is turned on or off.
                Toast.makeText(MainActivity.this, toastMessage,Toast.LENGTH_SHORT)
                        .show();

            }
        });

        //createNotificationChannel();
    }

    public void createNotificationChannel(){
        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
/*
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Stand up notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifies every 15 minutes to stand up and walk");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
*/

    }
}