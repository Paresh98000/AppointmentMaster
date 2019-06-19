package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class EditService extends Activity {

    TextView tv_sr_type,tv_desc,tv_ph,tv_Address,tv_error;
    EditText edt_name,edt_desc,edt_phn,edt_ct;
    Spinner sp_type;

    String services[];

    String userid,username;

    FirebaseSupport fs;
    SQLiteDatabase sql;

    HashMap<String,String> serviceData;

    String index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        fs = new FirebaseSupport(getApplicationContext(),this);

        sql = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

        tv_desc = findViewById(R.id.txt_desc_ser);
        tv_ph = findViewById(R.id.txt_phone_e_s);
        tv_Address = findViewById(R.id.txt_address_e_s);
        tv_sr_type = findViewById(R.id.txt_ser_e_s);
        tv_error = findViewById(R.id.txt_error_e_s);

        edt_name = findViewById(R.id.edit_name_e_s);
        edt_desc = findViewById(R.id.edit_ser_desc_e_s);

        sp_type = findViewById(R.id.spinner_serviceType_e_s);

        edt_phn = findViewById(R.id.edit_phone_e_s);
        edt_ct = findViewById(R.id.edit_s_city);

        services = new String[]{"No Data"};

        serviceData = new HashMap<>();

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

        Intent i = getIntent();

        index = i.getStringExtra("Clicked");

        c = sql.rawQuery("Select * from Service where Id=?",new String[]{index});

        if(c.getCount()>0){

            while(c.moveToNext()){
                int cnt = 0;
                while(cnt<c.getColumnCount()) {
                    if (cnt == 0) {
                        serviceData.put(c.getColumnName(cnt), c.getInt(cnt) + "");
                    } else {
                        serviceData.put(c.getColumnName(cnt), c.getString(cnt));
                    }
                    cnt++;
                }
            }
        }

        c.close();

        String provider = i.getStringExtra("Provider");
        if(!provider.equals(userid)){
            edt_phn.setEnabled(false);
            edt_desc.setEnabled(false);
            edt_ct.setEnabled(false);
            edt_name.setEnabled(false);
            sp_type.setEnabled(false);
            findViewById(R.id.layout_save_delete).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.txt_title_e_s)).setText("Service");
            (findViewById(R.id.btn_create_e_s)).setEnabled(false);
            (findViewById(R.id.btn_cancel_e_s)).setEnabled(false);
            edt_desc.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.txt_ser_desc_e_s)).setText(serviceData.get("Description"));
            ((TextView)findViewById(R.id.txt_ser_desc_e_s)).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.txt_ser_desc_e_s)).setSelected(true);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        edt_name.setText(serviceData.get("Name"));
        edt_ct.setText(serviceData.get("city"));
        edt_desc.setText(serviceData.get("Description"));
        edt_phn.setText(serviceData.get("Phone"));

        Cursor ser_c = sql.rawQuery("Select Name from ServiceType",null);

        if(ser_c.getCount()>0){
            int row = ser_c.getCount();
            services = new String[row];
            int i=0;
            while(ser_c.moveToNext()){
                services[i]=ser_c.getString(0);
                i++;
            }
        }

        //Toast.makeText(getApplicationContext(),serviceData.toString(),Toast.LENGTH_SHORT).show();

        ser_c.close();

        sp_type.setAdapter(new ArrayAdapter<>(getBaseContext(),R.layout.spinner_item_simple_text_view,R.id.txt_spinner_item_view,services));

        for(int x = 0;x<services.length;x++){
            //Toast.makeText(getApplicationContext(),"Matching : "+services[x]+" to "+serviceData.get("ServiceType"),Toast.LENGTH_SHORT).show();
            if(services[x].equals(serviceData.get("ServiceType"))){
                sp_type.setSelection(x);
                //Toast.makeText(getApplicationContext(),"Matched",Toast.LENGTH_SHORT).show();
                break;
            }
        }

    }

    public void onSaveService(View v){

        String name = edt_name.getText().toString();
        String phone = edt_phn.getText().toString();
        String city = edt_ct.getText().toString();
        String desc = edt_desc.getText().toString();
        String type = services[(int)sp_type.getSelectedItemId()];
        String provider = userid;
        String providerName = username;

        String error = "";


        if(name == null||name == ""){
            error += "invalid name\n";
        }
        if(phone == "" || phone.length() != 10){
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
            sd.setUserId(fs.userid);

            sd.setId(serviceData.get("Id"));

            long x = sql.update("Service", sd.getContentValue(), "Id=?", new String[]{index});
            fs.addService(sd.getMap());
            ContentValues val = new ContentValues();
            val.put("Id", sd.getId());
            val.put("change", "update");
            sql.insert("tmp_service", null, val);

            Toast.makeText(getApplicationContext(), "Successfully Updated service", Toast.LENGTH_SHORT).show();
            onBackPressed();

        }else{
            tv_error.setVisibility(View.VISIBLE);
            tv_error.setText(error);
        }
    }

    public void onDeleteService(View v){
        sql.delete("Service","Id=? and UserId=?",new String[]{index,fs.userid});
        fs.removeService(Integer.parseInt(index));
        onBackPressed();
    }
}