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

public class ServiceArrayAdapter extends ArrayAdapter implements AdapterView.OnItemClickListener {

    Activity context;
    int clicked;
    int layout;
    ArrayList<HashMap<String,String>> array;

    public ServiceArrayAdapter(@NonNull Activity context, int resource, ArrayList<HashMap<String,String>> arr) {
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

        ( (TextView)v.findViewById(R.id.txt_ser_i_name)).setText( array.get(position).get("Name") );
        ( (TextView)v.findViewById(R.id.txt_ser_i_city)).setText("City : "+ array.get(position).get("city") );
        ( (TextView)v.findViewById(R.id.txt_ser_i_sertype)).setText( "ServiceType : " + array.get(position).get("ServiceType") );
        return v;

    }

    @Override
    public int getCount() {
        return array.size();
    }

    public String getId(int pos){
        return array.get(pos).get("Id");
    }

    public String getProviderId(int p){
        return array.get(p).get("ServiceProvider");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(context.getBaseContext(),"Hey.. This is from adapter",Toast.LENGTH_SHORT).show();
    }
}
