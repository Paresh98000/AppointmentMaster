package spksolutions.appointment.appointmentmaster;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;

public class CalendarSupport {

    Context c;
    ContentResolver resolver;
    ContentValues vals;
    Uri uri = CalendarContract.Events.CONTENT_URI;
    String useremail;

    CalendarSupport(Context x) {
        c = x;
        resolver = c.getContentResolver();
        vals = new ContentValues();
        useremail = x.getSharedPreferences("user",Context.MODE_PRIVATE).getString("email","not found");
    }

    void createReminder( String titel, String text, String location, long timeinMilis1,long timeinMillis2) {

        /*Intent i = new Intent(c,AppointmentAlarm.class);
        i.putExtra("Title",titel);
        i.putExtra("Text",text);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP,timeinMilis,p);
        }*/
        vals.clear();
        vals.put(CalendarContract.Events.CALENDAR_ID,1);
        vals.put(CalendarContract.Events.TITLE, titel);
        vals.put(CalendarContract.Events.DESCRIPTION, text);
        vals.put(CalendarContract.Events.EVENT_LOCATION, location);
        vals.put(CalendarContract.Events.DTSTART, timeinMilis1);
        vals.put(CalendarContract.Events.DTEND, timeinMillis2);
        vals.put(CalendarContract.Events.STATUS,1);
        //vals.put(CalendarContract.Events.ALL_DAY, 0);
        Calendar cx = Calendar.getInstance();
        vals.put(CalendarContract.Events.EVENT_TIMEZONE, cx.getTimeZone().getDisplayName());
        Uri i = resolver.insert(CalendarContract.Events.CONTENT_URI,vals);
        Log.w("uri",i.toString());
        vals.clear();
        vals.put("event_id", Long.parseLong(i.getLastPathSegment()));
        //vals.put(CalendarContract.Reminders.TITLE, titel);
        //vals.put(CalendarContract.Reminders.DESCRIPTION, text);
        //vals.put(CalendarContract.Reminders.EVENT_LOCATION, location);
        //vals.put("dtstart", timeinMilis);
        //vals.put("allDay", 0);
        //vals.put("eventStatus", 1);
        //vals.put("hasAlarm", 1);
        vals.put("method",1);
        vals.put("minutes",10);

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //resolver.insert(CalendarContract.Reminders.CONTENT_URI, vals);
    }

    void deleteReminder(String id) {
        /*Intent i = new Intent(c,AppointmentAlarm.class);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(c,1,i,0);
        if (am != null) {
            am.cancel(p);
        }*/

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        resolver.delete(CalendarContract.Calendars.CONTENT_URI, "_id=?", new String[]{id});
    }

    void updateReminder(String id, String titel, String text, String location, long timeinMilis) {
        /*Intent i = new Intent(c,AppointmentAlarm.class);
        i.putExtra("Title",titel);
        i.putExtra("Text",text);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP,timeinMilis,p);
        }*/
        vals.clear();
        //vals.put("calendar_id",id);
        vals.put("_id", id);
        vals.put("title", titel);
        vals.put("description", text);
        vals.put("eventLocation", location);
        vals.put("dtstart", timeinMilis);
        vals.put("dtend", timeinMilis + 9000000);
        vals.put("allDay", 0);
        vals.put("eventStatus", 1);
        vals.put("hasAlarm", 1);
        vals.put("method", 1);
        vals.put("minutes", 5);
        Calendar cx = Calendar.getInstance();
        //vals.put("eventTimezone", cx.getTimeZone().toString());
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //resolver.update(uri, vals, "calendar_id=?", new String[]{id});
    }

    void createNotification(String title,String text,long time){
        Intent i = new Intent(c,AppointmentAlarm.class);
        i.putExtra("Title",title);
        i.putExtra("Text",text);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP,time,p);
        }
    }

    void deleteNotificatioin(){
        Intent i = new Intent(c,AppointmentAlarm.class);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(c,1,i,0);
        if (am != null) {
            am.cancel(p);
        }
    }

    void getCalendarList(){
        String[] projection = new String[] { "_id", "name" };
        Uri calendars = Uri.parse("content://calendar/calendars");

        Cursor managedCursor =
                managedQuery(calendars, projection,
                        "selected=1", null, null);

        if (managedCursor.moveToFirst()) {
            String calName;
            String calId;
            int nameColumn = managedCursor.getColumnIndex("name");
            int idColumn = managedCursor.getColumnIndex("_id");
            do {
                calName = managedCursor.getString(nameColumn);
                calId = managedCursor.getString(idColumn);
            } while (managedCursor.moveToNext());
        }
    }

    private Cursor managedQuery(Uri calendars, String[] projection, String s, Object o, Object o1) {
        ContentResolver r = c.getContentResolver();

        Cursor cur = r.query(calendars,projection,null,null,null);

        return cur;
    }

}
