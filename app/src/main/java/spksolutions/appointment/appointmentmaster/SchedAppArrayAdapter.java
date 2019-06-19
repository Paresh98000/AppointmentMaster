package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SchedAppArrayAdapter extends ArrayAdapter{

    Activity context;
    int clicked;
    int layout;
    ArrayList<HashMap<String,String>> array;
    SQLiteDatabase db;

    public SchedAppArrayAdapter(@NonNull Activity context, int resource, ArrayList<HashMap<String,String>> arr) {
        super(context.getBaseContext(), resource);
        this.context = context;
        layout = resource;
        array = arr;
        clicked=-1;
        db = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(layout,parent,false);

        ( (TextView)v.findViewById(R.id.txt_schapp_i_name)).setText( array.get(position).get("ServiceProvider") );
        ( (TextView)v.findViewById(R.id.txt_schapp_i_service)).setText(array.get(position).get("ServiceType") );
        ( (TextView)v.findViewById(R.id.txt_schapp_i_datetime)).setText(array.get(position).get("ADate") + " At " + array.get(position).get("ATime"));

        final Button img = (Button)v.findViewById(R.id.img_schapp_i_approve);

        final Button img1 = (Button)v.findViewById(R.id.img_schapp_i_refuse);

        String ap = array.get(position).get("Status");

        if(ap.equals("Approved")){
            img.setText(" - Approved - ");
            img1.setText("Refuse");
        }else if(ap.equals("Refused")){
            img1.setText(" - Refused - ");
            img.setText("Approve");
        }else{

        }

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(context.getBaseContext(),EditScheduleActivity.class);
                i.putExtra("Clicked",array.get(position).get("Id"));*/
                String s = array.get(position).get("Status");
                    array.get(position).remove("Status");
                    array.get(position).put("Status", "Approved");

                    FirebaseSupport fs = new FirebaseSupport(context.getBaseContext(), context);
                    fs.createDatabase();
                    ContentValues val = new ContentValues();
                    val.put("Status", "Approved");
                    db.update("Appoint", val, "Id=?", new String[]{array.get(position).get("Id")});
                    HashMap<String,String> x = array.get(position);
                    x.remove("SchedAppointId");
                    x.remove("providerId");
                    fs.addAppointment(x);

                Toast.makeText(context.getBaseContext(),"Successfully Approved",Toast.LENGTH_SHORT).show();
                //context.startActivity(i);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        img.setText("- Approved - ");
                        img1.setText("Refuse");
                    }
                });
            }
        });

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ind = array.get(position).get("Id");
                array.get(position).remove("Status");
                array.get(position).put("Status", "Refused");

                FirebaseSupport fs = new FirebaseSupport(context.getBaseContext(), context);
                ContentValues val = new ContentValues();

                val.put("Status", "Refused");
                fs.createDatabase();
                db.update("Appoint", val, "Id=?", new String[]{ind});

                val.clear();

                val.put("Id",ind);
                val.put("change","update");

                db.insert("tmp_appoint",null,val);

                val.clear();

                String id = array.get(position).get("SchedAppointId");

                db.delete("SchedAppoint","Id=?",new String[]{id});
                id = array.get(position).get("providerId") + "/"+id;

                fs.removeSchedAppoint(id);

                Toast.makeText(context.getBaseContext(),"Successfully Refused",Toast.LENGTH_SHORT).show();

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        img1.setText(" - Refused -");
                        img.setText("Approve");
                    }
                });

            }
        });

        return v;

    }

    @Override
    public int getCount() {
        return array.size();
    }

}
