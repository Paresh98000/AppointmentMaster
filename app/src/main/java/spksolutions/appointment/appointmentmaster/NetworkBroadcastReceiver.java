package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    boolean changed = false; int x=0;
    @Override
    public void onReceive(Context context, Intent intent) {

        //MainActivity activity = MainActivity.getInstance();
        //ConnectivityManager cm = (ConnectivityManager) activity.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo n = cm.getActiveNetworkInfo();
        /*if(n!=null&&n.isConnected())
            Toast.makeText(context,"ANY CHANGE DATA"+x++,Toast.LENGTH_SHORT).show();
*/
       /* final ConnectivityManager cm = (ConnectivityManager) activity.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                changed = true;
                int i = 1;
                NetworkInfo n;
                do {
                    n = cm.getActiveNetworkInfo();
                    if(n!=null && n.isConnected()){
                        activity.checkConnection();
                        activity.onUserReady();
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }while(i<10);
                changed = false;
            }
        });

        if(!changed){
            t.start();
        }*/
        //Toast.makeText(context,"ANY CHANGE DATA"+x++,Toast.LENGTH_SHORT).show();

    }
}
