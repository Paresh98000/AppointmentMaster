package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddServiceActivity extends Activity {

    TextView tv_sr_type,tv_desc,tv_ph,tv_Address,tv_error;
    EditText edt_name,edt_desc,edt_phn,edt_ct;
    Spinner sp_type;

    String services[];

    String userid,username;

    FirebaseSupport fs;
    SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_service);

        fs = new FirebaseSupport(getApplicationContext(),this);

        sql = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

        tv_desc = findViewById(R.id.txt_des_a_s);
        tv_ph = findViewById(R.id.txt_ph_a_s);
        tv_Address = findViewById(R.id.txt_add_a_s);
        tv_sr_type = findViewById(R.id.txt_s_type_a_s);
        tv_error = findViewById(R.id.txt_error_s);

        edt_name = findViewById(R.id.s_name);
        edt_desc = findViewById(R.id.s_desc);

        sp_type = findViewById(R.id.spinner_serviceType);

        edt_phn = findViewById(R.id.phone_ad_service);
        edt_ct = findViewById(R.id.s_city);

        services = new String[]{"No Data"};

        userid = getSharedPreferences("user",MODE_PRIVATE).getString("userId","No User");
        username = getSharedPreferences("user",MODE_PRIVATE).getString("userName","No User");

        Cursor c = sql.rawQuery("Select Name from ServiceType;",null);
        if(c.getCount()>0){
            services = new String[c.getCount()];
            int ind = 0;
            while(c.moveToNext()){
                services[ind]=c.getString(0);
                ind++;
            }
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_item_simple_text_view,R.id.txt_spinner_item_view,services);
        sp_type.setAdapter(adapter);

    }

    public void onCreateService(View v){

        String name = edt_name.getText().toString();
        String phone = edt_phn.getText().toString();
        String city = edt_ct.getText().toString();
        String desc = edt_desc.getText().toString();
        String type = services[(int)sp_type.getSelectedItemId()];
        String provider = userid;
        String providerName = username;

        String error = "";


        if(name.equals("")){

            error += "invalid name\n";
        }
        if(phone .equals("") || phone.length() != 10){

            error += "invalid phone";
        }
        if(city.length() == 0){

            error += "invalid city";
        }
        if(desc.length()==0){

            error += "enter description";
        }

        if(error.length()==0) {

            tv_error.setVisibility(View.GONE);

            ServiceData sd = new ServiceData(name, provider, desc, type, phone, city);
            sd.setStatus("Approved");

            Cursor c = sql.rawQuery("Select Id from Service order by id desc",null);

            int id = 0;
            c.moveToFirst();
            if(c.getCount()>0){
                id = c.getInt(0)+1;
            }

            Log.d(" AddService"," Id Of Service: "+id);

            sd.setId(id+"");
            sd.setUserId(userid);

            long x = sql.insert("Service", "Zipcode,Street,Country,State", sd.getContentValue());

            if(x>0) {

                ContentValues val = new ContentValues();
                val.put("Id", id);
                val.put("change","insert");
                sql.insert("tmp_service", null, val);
                fs.addService(sd.getMap());
            }

            Toast.makeText(getApplicationContext(),"Successfully Added service",Toast.LENGTH_SHORT).show();
            onBackPressed();

        }else{

            tv_error.setVisibility(View.VISIBLE);
            tv_error.setText(error);
            findViewById(R.id.scr_add_service).scrollTo(0,0);
        }
    }

    public void onCancelServiceCreate(View v){
        //onBackPressed();
    }

}
