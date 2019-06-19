package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;

public class ScheduleArrayAdapter extends ArrayAdapter{

    Activity context;
    int clicked;
    int layout;
    ArrayList<HashMap<String,String>> array;

    public ScheduleArrayAdapter(@NonNull Activity context, int resource, ArrayList<HashMap<String,String>> arr) {
        super(context.getBaseContext(), resource);
        this.context = context;
        layout = resource;
        array = arr;
        clicked=-1;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(layout,parent,false);

        ( (TextView)v.findViewById(R.id.txt_sch_i_name)).setText( array.get(position).get("Name") );
        ( (TextView)v.findViewById(R.id.txt_sch_i_dateFrom)).setText(array.get(position).get("DateFrom") );
        //( (TextView)v.findViewById(R.id.txt_sch_i_dateTo)).setText( "To : " + array.get(position).get("DateTo") );
        ( (TextView)v.findViewById(R.id.txt_sch_i_place)).setText(array.get(position).get("Place") );

        return v;

    }

    public String getId(int posi){
        return array.get(posi).get("Id");
    }

    @Override
    public int getCount() {
        return array.size();
    }


}
