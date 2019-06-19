package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditScheduleActivity extends Activity {


    private FirebaseSupport fs;

    private SQLiteDatabase db;

    DatePicker edt_date_f,edt_date_t;

    private EditText edt_name,edt_place,edt_desc;
    private TextView tv_date_f,tv_date_t,tv_place,tv_desc,tv_error;

    String userid,index;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        fs = new FirebaseSupport(getBaseContext(),this);

        db = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

        edt_name = findViewById(R.id.edit_name_e_sch);
        edt_date_f = findViewById(R.id.edit_datef_e_sch);
        edt_date_t = findViewById(R.id.edit_datet_e_sch);
        edt_place = findViewById(R.id.edit_place_e_sch);
        edt_desc = findViewById(R.id.edit_desc_e_sch);

        tv_date_f = findViewById(R.id.txt_date_from_e_sch);
        tv_date_t = findViewById(R.id.txt_date_to_e_sch);
        tv_place = findViewById(R.id.txt_place_e_sch);
        tv_desc = findViewById(R.id.txt_Description_e_sch);
        tv_error = findViewById(R.id.txt_error_e_sch);


        Intent ii = getIntent();
        index = ii.getStringExtra("Clicked");

        userid = getSharedPreferences("user",MODE_PRIVATE).getString("userId"," No User ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        String df,dt;
        Cursor c = db.rawQuery("Select * from Schedule where Id=? and UserId=?",new String[]{index,fs.userid});
        if(c.getCount()>0) {
            c.moveToFirst();

            edt_name.setText(c.getString(c.getColumnIndex("Name")));

            df = (c.getString(c.getColumnIndex("DateFrom")));
            dt = (c.getString(c.getColumnIndex("DateTo")));

            edt_desc.setText(c.getString(c.getColumnIndex("Description")));
            edt_place.setText(c.getString(c.getColumnIndex("Place")));

            String[] date = df.split("/");

            //edt_date_f.setMinDate(System.currentTimeMillis());
            int d = Integer.parseInt(date[0]);
            int m = Integer.parseInt(date[1]);
            int y = Integer.parseInt(date[2]);
            edt_date_f.updateDate(y,m,d);
            edt_date_t.setMinDate(System.currentTimeMillis());
            date = dt.split("/");
            d = Integer.parseInt(date[0]);
            m = Integer.parseInt(date[1]);
            y = Integer.parseInt(date[2]);
            edt_date_t.updateDate(y,m,d);
        }


    }

    public void onSaveSchedule(View v) {

        String error = "";

        String name, datef, datet = null, place, desc, datecrt = "";

        Calendar cur = Calendar.getInstance();

        name = edt_name.getText().toString();

        if (name.equals(null) || name.equals("")) {
            error += "invalid name\n";
        }

        datef = edt_date_f.getDayOfMonth() + "/" + edt_date_f.getMonth() + "/" + edt_date_f.getYear();

        datet = edt_date_t.getDayOfMonth() + "/" + edt_date_t.getMonth() + "/" + edt_date_t.getYear();

        int year = cur.get(Calendar.YEAR);
        int month = cur.get(Calendar.MONTH);
        int day = cur.get(Calendar.DATE);


        datecrt = day + "/" + month + "/" + year;

        place = edt_place.getText().toString();

        if (place.equals(null) || place.equals("")) {
            error += "invalid place\n";
        }

        desc = edt_desc.getText().toString();

        if (desc.equals(null) || desc.equals("")) {
            error += "invalid description\n";
        }

        if (error != "") {
            tv_error.setText(error);
            tv_error.setVisibility(View.VISIBLE);
            findViewById(R.id.scrl_e_sch).scrollBy(0, 0);
            return;
        } else {

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

            db.update("Schedule", sd.getContentValue(), "Id=?", new String[]{index});
            ContentValues cv = new ContentValues();
            cv.put("Id", sd.getId());
            cv.put("change", "update");
            db.insert("tmp_schedule", null, cv);
            fs.addSchedule(sd.getMap());
            Toast.makeText(getBaseContext(), "Schedule Added Successfully", Toast.LENGTH_SHORT).show();
        }

        onBackPressed();

    }

    public void onDeleteSchedule(View v){
        db.delete("schedule","(Id=? or Id=?)",new String[]{index,"0"});
        ContentValues cv = new ContentValues();
        cv.put("Id", index);
        cv.put("change", "delete");
        cv.put("userid", fs.userid);
        db.insert("tmp_schedule", null, cv);
        fs.removeSchedule(index);
        onBackPressed();

    }
}
