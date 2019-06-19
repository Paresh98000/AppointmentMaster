package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class EditAppointmentActivity extends Activity {

    DatePicker edt_date;
    TimePicker edt_time;

    EditText edt_nm,edt_place,edt_desc;
    TextView tv_ser,tv_ser_p,tv_date,tv_time,tv_place,tv_desc,tv_error;
    Spinner sp_ser,sp_ser_p;
    String[] services,ser_providers;
    private FirebaseSupport fs;
    private int index;

    SQLiteDatabase dblite;
    private boolean flag1;
    private String fond_pro_id;
    private String userid;
    private String username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_edit_appointment);

        Intent i = getIntent();

        index = Integer.parseInt(i.getStringExtra("Clicked"));

        edt_date = findViewById(R.id.edit_date_e_a);
        edt_time = findViewById(R.id.edit_time_e_a);
        edt_place = findViewById(R.id.edit_place_e_a);
        edt_desc = findViewById(R.id.edit_desc_e_a);
        edt_nm = findViewById(R.id.edit_name_e_a);

        tv_ser = findViewById(R.id.txt_service_e_a);
        tv_ser_p = findViewById(R.id.txt_service_provider_e_a);
        tv_date = findViewById(R.id.txt_date_e_a);
        tv_time = findViewById(R.id.txt_time_e_a);
        tv_place = findViewById(R.id.txt_place_e_a);
        tv_desc = findViewById(R.id.txt_desc_e_a);
        tv_error = findViewById(R.id.txt_error_e_a);

        sp_ser = findViewById(R.id.spinner_service_e_a);
        sp_ser_p = findViewById(R.id.spinner_service_provider_e_a);

        fs = new FirebaseSupport(getBaseContext(),this);

        dblite = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

        Cursor ser = dblite.rawQuery("Select Name from ServiceType",null);
        int ci=0;
        final String arr[] = new String[ser.getCount()];
        while(ser.moveToNext()){
            arr[ci] = ser.getString(0);
            ci++;
        }
        ser.close();

        services = arr;

        ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),R.layout.support_simple_spinner_dropdown_item,arr);
        sp_ser.setAdapter(adapter);
        boolean found=false;

        String date,time;

        Cursor c = dblite.rawQuery("Select * from Appoint where Id=?",new String[]{i.getStringExtra("Clicked")});
        int se = 0 ;

        if(c.moveToFirst()){



            date = (c.getString(c.getColumnIndex("ADate")));
            time = (c.getString(c.getColumnIndex("ATime")));

            edt_nm.setText(c.getString(c.getColumnIndex("Name")));
            String provider = c.getString(c.getColumnIndex("ServiceProvider"));
            se = 0 ;

            while(se<arr.length){
                if(c.getString(c.getColumnIndex("ServiceType")).equals(arr[se])){
                    found = true;
                    break;
                }
                se++;
            }
            if(found) {
                int posi_for_pro=0;
                sp_ser.setSelection(se);
                Cursor c1 = dblite.rawQuery("Select Name from Service where ServiceType=?", new String[]{arr[se]});
                String arr2[];
                if (c1.getCount() > 0) {
                    arr2 = new String[c1.getCount()];
                    int cnt = 0;
                    while (c1.moveToNext()) {

                        arr2[cnt] = c1.getString(0);

                        if(arr2[cnt].equals(provider)){
                            posi_for_pro = cnt;
                        }

                        cnt++;

                    }
                } else {
                    arr2 = new String[]{"No data found"};
                }
                ser_providers = arr2;
                c1.close();

                ArrayAdapter adapter1 = new ArrayAdapter(getBaseContext(),R.layout.support_simple_spinner_dropdown_item,arr2);
                sp_ser_p.setAdapter(adapter1);
                sp_ser_p.setSelection(posi_for_pro);

                String datearr[] = date.split("/");

                int day = Integer.parseInt(datearr[0]);
                int month = Integer.parseInt(datearr[1]);
                int year = Integer.parseInt(datearr[2]);

                edt_date.updateDate(year,month,day);

                String[] timearr = time.split(":");

                int hour = Integer.parseInt(timearr[0]);
                int minute = Integer.parseInt(timearr[1]);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    edt_time.setMinute(minute);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    edt_time.setHour(hour);
                }

                edt_place.setText(c.getString(c.getColumnIndex("Place")));
                edt_desc.setText(c.getString(c.getColumnIndex("Description")));

            }

            Calendar cdr = Calendar.getInstance();
            edt_date.setMinDate(cdr.getTimeInMillis());

            userid = getSharedPreferences("user",MODE_PRIVATE).getString("userId","No User");
            if(userid == null)
                userid = fs.userid;
            username = getSharedPreferences("user",MODE_PRIVATE).getString("userName","No User");

        }

        c.close();

        sp_ser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = dblite.rawQuery("Select Name from Service where ServiceType=? and UserId=?", new String[]{arr[position],userid});
                String arr2[];
                if (c.getCount() > 0 && c.moveToNext()) {
                    arr2 = new String[c.getCount()];
                    int cnt = 0;
                    while (c.moveToNext()) {
                        arr2[cnt] = c.getString(0);
                        cnt++;
                    }
                } else {
                    arr2 = new String[]{"No data found"};
                }
                ArrayAdapter adapter1 = new ArrayAdapter(getBaseContext(),R.layout.support_simple_spinner_dropdown_item,arr2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onSaveAppointment(View v){



        String name = edt_nm.getText().toString();

        String service = services[sp_ser.getSelectedItemPosition()];
        String s_provider = ser_providers[sp_ser_p.getSelectedItemPosition()];

        Cursor find_provider = dblite.rawQuery("Select ServiceProvider from Service where Name=? and ServiceType=?",new String[]{s_provider,service});

        fond_pro_id=null;

        if(find_provider.getCount()>0){
            find_provider.moveToFirst();
            fond_pro_id = find_provider.getString(0);
        }

        find_provider.close();

        String place = edt_place.getText().toString();
        String desc = edt_desc.getText().toString();

        Calendar date_a, cur;

        cur = Calendar.getInstance();
        date_a = Calendar.getInstance();

        cur.setTimeInMillis(System.currentTimeMillis());

        boolean apporoved = false;

        int year = edt_date.getYear();
        int month = edt_date.getMonth();
        int day = edt_date.getDayOfMonth();

        int hour = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = edt_time.getHour();
        }

        int minute = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            minute = edt_time.getMinute();
        }

        date_a.set(year, month, day, hour, minute);

        Log.w(" Dates ",""+cur.toString()+" "+date_a.toString());
        String error = "";

        if (name.equals("")) {
            error += "invalid name\n";

        }else if (place.equals("")) {
            error += "invalid place \n";

        } else if (desc.equals("")) {
            error += "invalid description \n";

        }

        boolean errr = false;

        if (error.equals( "")) {
            tv_error.setVisibility(View.GONE);
            errr=false;
        } else {
            tv_error.setText(error);
            ScrollView s = findViewById(R.id.scr_e_a);
            s.scrollTo(0, 0);
            tv_error.setVisibility(View.VISIBLE);
            errr = true;
        }

        if(!errr) {

            final AppointmentData data = new AppointmentData();

            data.setUid(fs.userid);
            data.setName(name);
            data.setApporoved(false);
            data.setDate_a(date_a);
            data.setDate_c(cur);
            data.setDesc(desc);
            data.setService(service);
            data.setPlace(place);
            data.setAid(index+"");

            flag1 = true;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if (ni != null && ni.isConnected()) {

                int x = dblite.update("Appoint",data.getGenerateContantValues(),"Id=?",new String[]{index+""});
                if(x>0) {
                    ContentValues v1 = new ContentValues();
                    v1.put("Id", data.getAid());
                    v1.put("change", "update");
                    dblite.insert("tmp_Appoint", null, v1);
                    v1.clear();
                    Cursor c = dblite.rawQuery("Select Id from schedappoint order by Id desc",null);
                    c.moveToFirst();
                    int id;
                    try {
                        id = c.getInt(0) + 1;
                    }catch(Exception e){
                        e.printStackTrace();
                        id=1;
                    }
                    v1.put("Id",id);
                    v1.put("appointId",data.getAid());
                    dblite.delete("SchedAppoint","appointId=?",new String[]{data.getAid()});
                    v1.put("clientId",data.getUid());
                    v1.put("providerId",fond_pro_id);
                    dblite.insert("SchedAppoint",null,v1);
                    Cursor c_dele = dblite.rawQuery("select Id from schedappoint where appointId=?",new String[]{data.getAid()});
                    if(c_dele.getCount()>0){
                        while(c_dele.moveToNext()){
                            fs.removeSchedAppoint(c_dele.getInt(0)+"");
                        }
                    }
                    c_dele.close();
                    v1.put("appointId",data.getAid());
                    dblite.delete("SchedAppoint","appointId=?",new String[]{data.getAid()});
                    v1.put("clientId",data.getUid());
                    v1.put("providerId",fond_pro_id);
                    dblite.insert("SchedAppoint",null,v1);
                    fs.addAppointment(data.generateData());
                    HashMap<String,String> datafs = new HashMap<>();
                    datafs.put("Id",id+"");
                    datafs.put("appointId",data.getAid());
                    dblite.delete("SchedAppoint","appointId=?",new String[]{data.getAid()});
                    datafs.put("clientId",data.getUid());
                    datafs.put("providerId",fond_pro_id);
                    fs.addScheduleAppoint(datafs);
                    c.close();
                }
                Toast.makeText(getApplicationContext(), "Appoitment Succefully Added", Toast.LENGTH_LONG).show();
                onBackPressed();

            } else {

                Toast.makeText(getApplicationContext(), "No Connection found", Toast.LENGTH_LONG).show();
                final AlertDialog.Builder alert1 = new AlertDialog.Builder(this);

                alert1.setMessage("No network found");

                alert1.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        if (ni != null && ni.isConnected()) {
                            int x = dblite.update("Appoint",data.getGenerateContantValues(),"Id=?",new String[]{index+""});
                            if(x>0) {
                                ContentValues v1 = new ContentValues();
                                v1.put("Id", data.getAid());
                                v1.put("change", "update");
                                dblite.insert("tmp_Appoint", null, v1);
                                v1.clear();
                                Cursor c = dblite.rawQuery("Select count(*) from schedappoint",null);
                                c.moveToFirst();
                                String id = c.getInt(0)+"";
                                v1.put("Id",id);
                                Cursor c_dele = dblite.rawQuery("select Id from schedappoint where appointId=?",new String[]{data.getAid()});
                                if(c_dele.getCount()>0){
                                    while(c_dele.moveToNext()){
                                        fs.removeSchedAppoint(c_dele.getInt(0)+"");
                                    }
                                }
                                c_dele.close();
                                dblite.delete("schedappoint","appointId=?",new String[]{data.getAid()});
                                v1.put("appointId",data.getAid());
                                dblite.delete("SchedAppoint","appointId=?",new String[]{data.getAid()});
                                v1.put("clientId",data.getUid());
                                v1.put("providerId",fond_pro_id);
                                dblite.insert("SchedAppoint",null,v1);
                                fs.addAppointment(data.generateData());
                                c.close();
                                HashMap<String,String> datafs = new HashMap<>();
                                datafs.put("Id",id+"");
                                datafs.put("appointId",data.getAid());
                                dblite.delete("SchedAppoint","appointId=?",new String[]{data.getAid()});
                                datafs.put("clientId",data.getUid());
                                datafs.put("providerId",fond_pro_id);
                                fs.addScheduleAppoint(datafs);
                            }
                            onBackPressed();
                        } else {
                            Toast.makeText(getApplicationContext(), "No Connection found", Toast.LENGTH_LONG).show();

                            alert1.create().show();
                        }
                    }
                });

                alert1.setNegativeButton("Save Offline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        flag1 = false;
                        dblite.update("Appoint", data.getGenerateContantValues(), "Id=?", new String[]{index + ""});

                        ContentValues v1 = new ContentValues();
                        v1.put("Id", data.getAid());
                        v1.put("change", "update");
                        dblite.insert("tmp_Appoint", null, v1);
                        v1.clear();
                        Cursor c = dblite.rawQuery("Select count(*) from schedappoint",null);
                        c.moveToFirst();
                        String id = c.getInt(0)+"";
                        v1.put("Id",id);
                        v1.put("appointId",data.getAid());
                        v1.put("clientId",data.getUid());
                        v1.put("providerId",fond_pro_id);
                        dblite.insert("SchedAppoint",null,v1);
                        fs.addAppointment(data.generateData());
                        Cursor c_dele = dblite.rawQuery("select Id from schedappoint where appointId=?",new String[]{data.getAid()});
                        if(c_dele.getCount()>0){
                            while(c_dele.moveToNext()){
                                fs.removeSchedAppoint(c_dele.getInt(0)+"");
                            }
                        }
                        c_dele.close();
                        dblite.delete("schedappoint","appointId=?",new String[]{data.getAid()});
                        v1.put("appointId",data.getAid());
                        v1.put("clientId",data.getUid());
                        v1.put("providerId",fond_pro_id);
                        dblite.insert("SchedAppoint",null,v1);
                        HashMap<String,String> datafs = new HashMap<>();
                        datafs.put("Id",id+"");
                        datafs.put("appointId",data.getAid());
                        datafs.put("clientId",data.getUid());
                        datafs.put("providerId",fond_pro_id);
                        fs.addScheduleAppoint(datafs);
                        onBackPressed();
                    }
                });

                alert1.create().show();

            }
        }

    }

    public void onDeleteAppointment(View v){

        int x = dblite.delete("Appoint","Id=? and UserId=?",new String[]{index+"",userid});
        if (x > 0) {
            ContentValues v1 = new ContentValues();
            v1.put("Id",index);
            v1.put("change","delete");
            dblite.insert("tmp_Appoint", null, v1);
            fs.removeAppointment(index+"");
            Toast.makeText(getBaseContext(),"Deleted Successfully",Toast.LENGTH_SHORT).show();
        }
        onBackPressed();
    }

    String getUserId(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert cm!=null;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        String x;
        if(ni.isConnected()) {
            x = FirebaseAuth.getInstance().getUid();
        }
        else{
            x = FirebaseAuth.getInstance().getUid();
            if(x==null)
                x = "No user found";
        }
        return x;
    }
}
