package spksolutions.appointment.appointmentmaster;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class AppointmentAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context,MainActivity.class);
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        NotificationCompat.Builder n = new NotificationCompat.Builder(context,"AppointmentMaster");
        n.setContentTitle(intent.getStringExtra("Title"));
        n.setContentText(intent.getStringExtra("Text"));
        n.setSmallIcon(R.mipmap.appointmentmaster);
        n.setStyle(new NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line..."));
        n.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        CharSequence name = "Appointment Master";
        String description = "Appointment Master Description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("AppoinmentMaster", name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm1 = (NotificationManager) context.getSystemService(   Context.NOTIFICATION_SERVICE);
            nm1.createNotificationChannel(channel);
        }
        TaskStackBuilder t = TaskStackBuilder.create(context);
        t.addParentStack(MainActivity.class);
        t.addNextIntent(i);
        PendingIntent p = t.getPendingIntent(0,0);
        n.setContentIntent(p);
        if (nm != null) {
            nm.notify(10,n.build());
        }
    }
}
