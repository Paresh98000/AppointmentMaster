package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AppointArrayAdapter extends ArrayAdapter {

    Activity context;
    int clicked;
    int layout;
    ArrayList<HashMap<String,String>> array;
    String status;
    CalendarSupport cs;

    public AppointArrayAdapter(@NonNull Activity context, int resource, ArrayList<HashMap<String,String>> arr) {
        super(context.getBaseContext(), resource);
        this.context = context;
        layout = resource;
        array = arr;
        clicked=-1;
        cs = new CalendarSupport(context.getBaseContext());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(layout,parent,false);

        ( (TextView)v.findViewById(R.id.txt_a_i_name)).setText( array.get(position).get("Name") );
        ( (TextView)v.findViewById(R.id.txt_a_i_date)).setText("Date : " + array.get(position).get("ADate") );
        ( (TextView)v.findViewById(R.id.txt_a_i_time)).setText( "Time : " + array.get(position).get("ATime") );
        ( (TextView)v.findViewById(R.id.txt_a_i_approved)).setText( "Status : " + array.get(position).get("Status") );
        ( (TextView)v.findViewById(R.id.txt_a_i_place)).setText( "Place : " + array.get(position).get("Place") );

        status = array.get(position).get("Status") ;


        ( (ImageView)v.findViewById(R.id.img_a_i_edit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status.equals("Approved")){
                    String date1[] = array.get(position).get("ADate").split("/");
                    String time1[] = array.get(position).get("ATime").split(":");
                    int year,month,date;
                    short hour,minute;

                    date = Integer.parseInt(date1[0]);
                    month = Integer.parseInt(date1[1]);
                    year = Integer.parseInt(date1[2]);

                    hour = Short.parseShort(time1[0]);
                    minute = Short.parseShort(time1[1]);

                    Calendar c = Calendar.getInstance();
                    c.set(year,month,date,hour,minute);

                    //cs.createReminder(array.get(position).get("Name"),array.get(position).get("Description"),array.get(position).get("Place"),c.getTimeInMillis(),c.getTimeInMillis());

                    Intent cal = new Intent(Intent.ACTION_EDIT, CalendarContract.Events.CONTENT_URI);

                    cal.putExtra(CalendarContract.Events.TITLE,array.get(position).get("Name"));
                    cal.putExtra(CalendarContract.Events.DESCRIPTION,array.get(position).get("Description"));
                    cal.putExtra(CalendarContract.Events.DTSTART,c.getTimeInMillis());
                    cal.putExtra(CalendarContract.Events.DTEND,c.getTimeInMillis());
                    cal.putExtra(CalendarContract.Events.EVENT_TIMEZONE,c.getTimeZone().getDisplayName());

                    context.startActivityForResult(cal,230);

                }else{
                    Toast.makeText(context.getBaseContext(),"Appointment is not approved",Toast.LENGTH_LONG).show();
                }
            }
        });
        return v;

    }

    public String getIndex(int position){
        return array.get(position).get("Id");
    }

    @Override
    public int getCount() {
        return array.size();
    }

}
