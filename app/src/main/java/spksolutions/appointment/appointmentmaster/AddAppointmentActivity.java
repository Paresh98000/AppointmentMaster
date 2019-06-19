package spksolutions.appointment.appointmentmaster;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddAppointmentActivity extends Activity {

    FirebaseSupport fs;

    DatePicker date__a;
    TimePicker time__a;

    EditText edt_nm,edt_place,edt_desc;
    TextView tv_ser,tv_ser_p,tv_date,tv_time,tv_place,tv_desc,tv_error;
    Spinner sp_ser,sp_ser_p;
    String[] services,ser_providers;
    boolean fetchingservice;
    boolean flag1;
    private Thread fetch;
    private Thread user_info;
    String userid,username,index;

    SQLiteDatabase mSQLite;

    @Override
    protected void onCreate(Bundle savedInstanceState){



        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.acivity_add_appointment);

        fs = new FirebaseSupport(getApplicationContext(),this);

        get_A_Data(MainActivity.isConnected);

        edt_nm = findViewById(R.id.edit_name_a);



        userid = getSharedPreferences("user",MODE_PRIVATE).getString("userId","No User");
        username = getSharedPreferences("user",MODE_PRIVATE).getString("userName","No User");

        sp_ser = findViewById(R.id.spinner_service_a);
        sp_ser_p = findViewById(R.id.spinner_service_provider_a);

        date__a = findViewById(R.id.edit_date_a);
        time__a = findViewById(R.id.edit_time_a);
        edt_place = findViewById(R.id.edit_place_a);
        edt_desc = findViewById(R.id.edit_desc_a);

        tv_ser = findViewById(R.id.txt_service_a);
        tv_ser_p = findViewById(R.id.txt_service_provider_a);
        tv_date = findViewById(R.id.txt_date_a);
        tv_time = findViewById(R.id.txt_time_a);
        tv_place = findViewById(R.id.txt_place_a);
        tv_desc = findViewById(R.id.txt_desc_a);
        tv_error = findViewById(R.id.txt_error_a);

        services = new String[]{"no data found"};
        mSQLite = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

        fetchingservice = false;

        //Log.w("Service",""+services[0]);

        user_info = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!fetchingservice){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_ser.setText("Service Type : fetching data");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_ser.setText("Service Type : fetching data.");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_ser.setText("Service Type : fetching data..");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_ser.setText("Service Type : fetching data...");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(Thread.interrupted()){
                        break;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_ser.setText("Service Type : ");
                    }
                });
            }
        });

        user_info.start();

        fetch = new Thread(new Runnable() {
            @Override
            public void run() {

                int a = getServicesTypes();
                //Log.w("Finally","Len: "+"");


                Cursor c_st = mSQLite.rawQuery("Select Name,Id from ServiceType",null);

                if(c_st.getCount()>0){
                    services = new String[c_st.getCount()];
                    int count = 0;
                    while(c_st.moveToNext()){
                        services[count]= c_st.getString(0);
                        count++;
                        Log.w(" Service Id",""+c_st.getInt(1));
                    }
                }

                final ArrayAdapter aa = new ArrayAdapter(getBaseContext(),R.layout.support_simple_spinner_dropdown_item,services);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sp_ser.setAdapter(aa);
                        sp_ser.setSelection(0);
                    }
                });

                fetchingservice = true;
            }
        });

        sp_ser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor c_pr = mSQLite.rawQuery("Select Name from Service where ServiceType=?",new String[]{services[position]});
                if(c_pr.getCount()>0){
                    ser_providers = new String[c_pr.getCount()];
                    int coun = 0;
                    while(c_pr.moveToNext()){
                        ser_providers[coun] = c_pr.getString(0);
                        coun++;
                    }

                }else{
                    ser_providers = new String[]{"No data found"};
                }

                final ArrayAdapter aa = new ArrayAdapter(getBaseContext(),R.layout.support_simple_spinner_dropdown_item,ser_providers);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sp_ser_p.setAdapter(aa);
                        sp_ser_p.setSelection(0);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        date__a.setMinDate(System.currentTimeMillis());
        time__a.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            time__a.setHour(cal.get(Calendar.HOUR_OF_DAY));
            time__a.setMinute(cal.get(Calendar.MINUTE));
        }
        fetch.start();

    }

    private void get_A_Data(boolean isConnected) {
    }

    private int getServicesTypes() {
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(fetch !=null && fetch.isInterrupted()){
            fetch.start();
        }

        if(user_info!=null&& user_info.isInterrupted()){
            user_info.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(fetch.isAlive()){
            fetch.interrupt();
        }
        if(user_info.isAlive()){
            user_info.interrupt();
        }
    }

    public boolean onCreateAppointment(View v) {

        String name = edt_nm.getText().toString();

        String service = services[sp_ser.getSelectedItemPosition()];
        String s_provider = ser_providers[sp_ser_p.getSelectedItemPosition()];

        String place = edt_place.getText().toString();
        String desc = edt_desc.getText().toString();

        Calendar date_a, cur;

        cur = Calendar.getInstance();
        date_a = Calendar.getInstance();

        boolean apporoved = false;


            int year = date__a.getYear();
            int month = date__a.getMonth();
            int day = date__a.getDayOfMonth();

            int hour = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = time__a.getHour();
            }
            int minute = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                minute = time__a.getMinute();
            }

            date_a.set(year, month, day, hour, minute);



        String error = "";

        if (name.equals("")) {
            error += "invalid name\n";

        }else if (place.equals("")) {
            error += "invalid place \n";

        } else if (desc.equals("")) {
            error += "invalid description \n";

        }


        if (error.equals("")) {
            tv_error.setVisibility(View.GONE);
        } else {
            tv_error.setText(error);
            ScrollView s = findViewById(R.id.scrl);
            s.scrollTo(0, 0);
            tv_error.setVisibility(View.VISIBLE);
            return false;
        }



        final AppointmentData data = new AppointmentData();

        data.setUid(getCurrenctUserId());

        data.setName(name);
        data.setApporoved(false);
        data.setDate_a(date_a);
        data.setDate_c(cur);
        data.setDesc(desc);
        data.setService(service);
        data.setPlace(place);
        data.setStatus("Not Approved");
        data.setS_provider(s_provider);
        data.setUid(fs.userid);
        int a_id=0;
        Cursor val = mSQLite.rawQuery("Select Max(Id) from appoint",null);
        if(val.getCount()>0){
            val.moveToFirst();
            int x = val.getInt(0);
            a_id = x + 1;

        }
        val.close();
        flag1 = true;
        data.setAid(a_id+"");
        data.setUserId(userid);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {

            mSQLite.insert("Appoint", null, data.getGenerateContantValues());
            ContentValues valx = new ContentValues();
            int id=0;
            Cursor c = mSQLite.rawQuery("Select Id from SchedAppoint order by Id desc",null);
            if(c.getCount()>0){
                c.moveToFirst();
                id = c.getInt(0)+1;
            }
            c.close();

            Cursor find_provider = mSQLite.rawQuery("Select ServiceProvider from Service where Name=? and ServiceType=?",new String[]{s_provider,service});

            String fond_pro_id=null;

            if(find_provider.getCount()>0){
                find_provider.moveToFirst();
                fond_pro_id = find_provider.getString(0);
            }

            find_provider.close();

            data.setS_provider(fond_pro_id);
            valx.clear();
            valx.put("Id",id);
            valx.put("appointId",a_id);
            valx.put("clientId",fs.userid);
            valx.put("providerId",data.getS_provider());
            mSQLite.insert("SchedAppoint",null,valx);
            ContentValues vl = new ContentValues();
            vl.put("Id",data.getAid());
            vl.put("change","insert");
            mSQLite.insert("tmp_Appoint",null,vl);

            fs.addAppointment(data.generateData());

            HashMap<String,String> m = new HashMap<>();

            m.put("Id",id+"");
            m.put("appointId",a_id+"");
            m.put("clientId",data.getUid());
            m.put("providerId",fond_pro_id);

            fs.addScheduleAppoint(m);

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
                        mSQLite.insert("Appoint", null, data.getGenerateContantValues());
                        ContentValues vl = new ContentValues();
                        vl.put("Id",data.getAid());
                        vl.put("change","insert");
                        mSQLite.insert("tmp_Appoint",null,vl);
                        ContentValues valx = new ContentValues();
                        int id=0;
                        Cursor c = mSQLite.rawQuery("Select count(*) from SchedAppoint",null);
                        if(c.getCount()>0){
                            id = c.getInt(0)+1;
                        }
                        c.close();
                        valx.put("Id",id);
                        valx.put("appointId",data.getAid());
                        valx.put("clientId",data.getUid());
                        valx.put("providerId",data.getS_provider());
                        mSQLite.insert("SchedAppoint",null,valx);
                        fs.addAppointment(data.generateData());
                        HashMap<String,String> m = new HashMap<>();

                        m.put("Id",id+"");
                        m.put("appointId",data.getAid());
                        m.put("clientId",data.getUid());
                        m.put("providerId",data.getS_provider());
                        fs.addScheduleAppoint(m);
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
                    mSQLite.insert("Appoint", null, data.getGenerateContantValues());
                    ContentValues vl = new ContentValues();
                    vl.put("Id",data.getAid());
                    vl.put("change","insert");
                    mSQLite.insert("tmp_Appoint",null,vl);
                    ContentValues valx = new ContentValues();
                    int id=0;
                    Cursor c = mSQLite.rawQuery("Select count(*) from SchedAppoint",null);
                    c.moveToFirst();
                    if(c.getCount()>0){
                        id = c.getInt(0)+1;
                    }
                    c.close();
                    valx.put("Id",id);
                    valx.put("appointId",data.getAid());
                    valx.put("clientId",data.getUid());
                    valx.put("providerId",data.getS_provider());

                    mSQLite.insert("SchedAppoint",null,valx);

                    fs.addAppointment(data.generateData());
                    HashMap<String,String> m = new HashMap<>();

                    m.put("Id",id+"");
                    m.put("appointId",data.getAid());
                    m.put("clientId",data.getUid());
                    m.put("providerId",data.getS_provider());

                    fs.addScheduleAppoint(m);

                    onBackPressed();
                }
            });
            alert1.create().show();
        }

        CalendarSupport cs = new CalendarSupport(getBaseContext());

        //cs.createReminder(data.getName(),data.getDesc(),data.getPlace(),cur.getTimeInMillis(),cur.getTimeInMillis());

        return true;
    }

    public void onCancelNewAppointment(View v){
        onBackPressed();
    }

    public String getCurrenctUserId() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        String s=null;

        if(auth !=null && user != null){
            s = user.getUid();
        }

        if(s==null){
            s="No User found";
        }

        return s;
    }
}
