package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddScheduleActivity extends Activity {

    private FirebaseSupport fs;

    private SQLiteDatabase db;

    DatePicker edt_date_f,edt_date_t;

    private EditText edt_name,edt_place,edt_desc;
    private TextView tv_date_f,tv_date_t,tv_place,tv_desc,tv_error;

    String userid,username;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_schedule);

        fs = new FirebaseSupport(getBaseContext(),this);

        db = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

        edt_name = findViewById(R.id.edit_name_schedule);
        edt_date_f = findViewById(R.id.edit_date_from_sch);
        edt_date_t = findViewById(R.id.edit_date_to_sch);
        edt_place = findViewById(R.id.edit_place_sch);
        edt_desc = findViewById(R.id.edit_desc_sch);

        tv_date_f = findViewById(R.id.txt_date_from_sch);
        tv_date_t = findViewById(R.id.txt_date_to_sch);
        tv_place = findViewById(R.id.txt_place_sch);
        tv_desc = findViewById(R.id.txt_Description_sch);
        tv_error = findViewById(R.id.txt_error_sch);

        Calendar cd = Calendar.getInstance();

        edt_date_t.setMinDate(cd.getTimeInMillis());
        edt_date_f.setMinDate(cd.getTimeInMillis());

        userid = getSharedPreferences("user",MODE_PRIVATE).getString("userId","No User");
        username = getSharedPreferences("user",MODE_PRIVATE).getString("userName","No User");

    }

    public void onCreateSchedule(View v){

        String index,name,datef,datet = null,place,desc,datecrt="";

        String error  = "";

        Cursor c = db.rawQuery("Select Id from Schedule where userid=? order by Id desc",new String[]{userid});

        index = "0";

        if(c.getCount()>0) {
            c.moveToFirst();


            index = (c.getInt(0) + 1) + "";
        }

        c.close();

        Calendar cur = Calendar.getInstance();
        name = edt_name.getText().toString();

        if(name.equals(null)||name.equals("")){
            error+="invalid name\n";
        }

                int year = edt_date_f.getYear();
                int month = edt_date_f.getMonth();
                int day = edt_date_f.getDayOfMonth();

                datef = day+"/"+month+"/"+year;



            datet = edt_date_t.getDayOfMonth()+"/"+edt_date_t.getMonth()+"/"+edt_date_t.getYear();

            year = cur.get(Calendar.YEAR);
            month = cur.get(Calendar.MONTH);
            day = cur.get(Calendar.DATE);


            datecrt = day+"/"+month+"/"+year;;


        place = edt_place.getText().toString();

        if(place.equals("")){
            error+="invalid place\n";
        }

        desc = edt_desc.getText().toString();

        if(desc.equals("")){
            error+="invalid description\n";
        }
        
        if(!error.equals("")){
            tv_error.setText(error);
            tv_error.setVisibility(View.VISIBLE);
            findViewById(R.id.scr_a_sch).scrollTo(0,0);
            return;
        }else {
            tv_error.setText("");
            tv_error.setVisibility(View.GONE);

            ScheduleData sd = new ScheduleData();

            sd.setId(index);
            sd.setDateCrt(datecrt);
            sd.setDescription(desc);
            sd.setPlace(place);
            sd.setDateTo(datet);
            sd.setDateFrom(datef);
            sd.setName(name);
            sd.setUserId(fs.userid);

            db.insert("Schedule", null, sd.getContentValue());
            ContentValues cv = new ContentValues();
            cv.put("Id", sd.getId());
            cv.put("change", "insert");
            db.insert("tmp_schedule", null, cv);
            fs.addSchedule(sd.getMap());
            Toast.makeText(getBaseContext(),"Schedule Added Successfully",Toast.LENGTH_SHORT).show();
        }




        onBackPressed();
    }

    public void onCancelSchedule(View v){
        onBackPressed();
    }
}
