package spksolutions.appointment.appointmentmaster;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class FirebaseSupport {

    private FirebaseAuth auth;
    private FirebaseDatabase data;
    DatabaseReference reference;
    FirebaseUser user;

    Activity activity;
    Context context;

    public SQLiteDatabase lite;

    boolean isConnected;

    String userid;

    HashMap<String, String> list;
    private int cntx;
    private long counter;

    FirebaseSupport(Context context, Activity activity) {

        data = FirebaseDatabase.getInstance();
        reference = data.getReference();
        auth = FirebaseAuth.getInstance();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            isConnected = true;

        }



        this.context = context;
        this.activity = activity;

        list = new HashMap<>();

        userid = activity.getSharedPreferences("user", Context.MODE_PRIVATE).getString("userId", "No User");
    }

    HashMap<String, String> getAppoint() {

        return list;
    }

    void SynchronizeDatabase() {

        //checking for tables

        if (checkConnection()) {

            String userid = auth.getCurrentUser().getUid();


            //lite.delete("ServiceType", "Id >= ?", new String[]{"null"});
            //Cursor c = lite.rawQuery("Select count(*) from ServiceType", null);
            //if (c.getCount() == 0) {


            //c.close();
            // This is for Appointment's Synchronisation

            syncAppointments();
            syncService();
            syncSchedule();

        }
    }

    public void syncAppointments() {
        if (checkConnection()) {

            if (lite == null) {
                createDatabase();
            } else {

                Cursor c_appoint_tmp = lite.rawQuery("Select Id from tmp_appoint where UserID=?", new String[]{userid});

                if (c_appoint_tmp.getCount() > 0) {

                    Cursor cur = lite.rawQuery("Select Id,change from tmp_appoint where UserId=?;", new String[]{userid});

                    while (cur.moveToNext()) {

                        String tmp = cur.getString(1);

                        if (tmp.equals("delete")) {

                            reference.child("/Appointment/" + userid + "/" + cur.getInt(0) + "").removeValue();

                        } else {

                            Cursor t_cur = lite.rawQuery("Select * from Appoint Where Id=? and userid=?", new String[]{cur.getInt(0) + "",userid});

                            if (t_cur.getCount() > 0) {
                                while (t_cur.moveToNext()) {
                                    list.clear();
                                    int i = 0;
                                    while (i < t_cur.getColumnCount()) {
                                        if (i == 0) {
                                            list.put(t_cur.getColumnName(i), t_cur.getInt(i) + "");
                                        } else {
                                            list.put(t_cur.getColumnName(i), t_cur.getString(i));
                                        }
                                        i++;
                                    }
                                    reference.child("/Appointment/" + cur.getInt(0) + "").setValue(list);
                                }
                                t_cur.close();
                            }
                        }
                    }
                    lite.delete("tmp_appoint", null, null);
                    cur.close();
                    c_appoint_tmp.close();


                } else {
                    Cursor t_cur = lite.rawQuery("Select * from Appoint where userid=?", new String[]{userid});
                    if (t_cur.getCount() > 0) {
                        while (t_cur.moveToNext()) {
                            int i = 0;
                            list.clear();
                            while (i < t_cur.getColumnCount()) {
                                if (i == 0) {
                                    list.put(t_cur.getColumnName(i), t_cur.getInt(i) + "");
                                } else {
                                    list.put(t_cur.getColumnName(i), t_cur.getString(i));
                                }
                                i++;
                            }
                            reference.child("/Appointment/" + t_cur.getInt(0) + "").setValue(list);
                        }
                    }
                    Log.w(" List", list.toString());
                    t_cur.close();
                }

                reference.child("/Appointment").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String id = userid;

                        long c = dataSnapshot.getChildrenCount();
                        int i = 0;

                        ArrayList<Object> obj1 = (ArrayList<Object>) dataSnapshot.getValue();

                        while (obj1 != null && i < obj1.size()) {

                            list = (HashMap<String, String>) obj1.get(i);

                            if (list != null) {

                                Cursor cc = lite.rawQuery("Select Id from Appoint where Id=?", new String[]{list.get("Id")});

                                if (cc.getCount() == 0) {

                                    ContentValues vals = new ContentValues();
                                    vals.put("Id", list.get("Id"));
                                    vals.put("ServiceProvider", list.get("ServiceProvider"));
                                    vals.put("ServiceType", list.get("ServiceType"));
                                    vals.put("DateCrt", list.get("DateCrt"));
                                    vals.put("ADate", list.get("ADate"));
                                    vals.put("ATime", list.get("ATime"));
                                    vals.put("Place", list.get("Place"));
                                    vals.put("Description", list.get("Description"));
                                    vals.put("Status", list.get("Status"));
                                    vals.put("Name", list.get("Name"));

                                    lite.insert("Appoint", null, vals);

                                }
                                cc.close();
                            }
                            i++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

    }

    void cleanTables() {
        String tables[] = new String[]{"Appoint", "Service", "schedule", "User"};
        if (lite == null) {
            createDatabase();
        } else {
            for (String x : tables) {
                lite.delete(x,"UserId=? or UserId=?",new String[]{"null",""});
                lite.delete(x, "Id=? or ( userid like ? or userid like ? or userid = ? or userid like ?)", new String[]{"null","No data", "", "null", "no user found"});
                lite.execSQL("drop table if exists "+x+"_tmp;");
                lite.execSQL("Create table " + x + "_tmp as  Select * from " + x + " group by Id order by id;");
                lite.execSQL("Drop table " + x + ";");

                lite.execSQL("Alter table " + x + "_tmp Rename to " + x + ";");

            }
        }
        tables = new String[]{"ServiceType", "schedappoint",};
        if (lite == null) {
            createDatabase();
        } else {
            for (String x : tables) {

                lite.delete(x, "id=?", new String[]{""});
                lite.execSQL("drop table if exists "+x+"_tmp;");
                lite.execSQL("Create table " + x + "_tmp as  Select * from " + x + " group by Id order by id;");
                lite.execSQL("DROP TABLE " + x + ";");
                try {
                    lite.execSQL("Alter table " + x + "_tmp Rename to " + x + ";");
                }catch(Exception e){
                    e.printStackTrace();
                    lite.execSQL("DROP TABLE " + x + "");
                    lite.execSQL("Alter table " + x + "_tmp Rename to " + x + ";");
                }

            }
        }
        String paths[] = {"/Appointment/" + userid, "/Schedule/" + userid, "/Service", "/SchedAppoint"};
        if (checkConnection()) {
            for (String x : paths) {
                reference.child(x + "/" + "null").removeValue();
            }
        }

    }

    void syncServiceType() {


        if (checkConnection()) {
            if (lite != null) {

                cntx = 0;
                reference.child("/ServiceType").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long con = dataSnapshot.getChildrenCount();
                        cntx = (int) con;

                        ContentValues list1 = new ContentValues();
                        ArrayList<Object> obj = (ArrayList<Object>) dataSnapshot.getValue();

                        if (obj != null) {
                            Log.w(" Data from", "Service Type" + obj.toString());
                            int i = 1;
                            while (i <= con) {
                                list1.clear();

                                if (obj != null) {

                                    String val = (String) obj.get(i);
                                    Cursor c = lite.rawQuery("Select * from ServiceType where name=?;", new String[]{val});
                                    if (c.getCount() == 0) {
                                        list1.put("Id", i);
                                        list1.put("Name", val);
                                        lite.insert("ServiceType", null, list1);
                                    }
                                }
                                i++;
                            }

                            }
                        }



                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if(cntx==0){
                    Cursor cr= lite.rawQuery("Select * from ServiceType;",null);
                    while(cr.moveToNext()) {
                        reference.child("/ServiceType/" +cr.getInt(0)).setValue(cr.getString(1));
                    }
                }

            }
        }else{
            Toast.makeText(context,"No Connection",Toast.LENGTH_SHORT).show();
        }


    }



    public void syncService(){

        if(checkConnection()) {

           {
               if (lite == null) {
                   createDatabase();
               }

               lite.delete("Service", "UserId=? or UserId=?", new String[]{"null", ""});

                Cursor c_service_tmp = lite.rawQuery("Select Id from tmp_service where UserId=?", new String[]{userid});

                if (c_service_tmp.getCount() > 0) {
                    Cursor cur = lite.rawQuery("Select Id,change from tmp_service where userID=?;", new String[]{userid});

                    while (cur.moveToNext()) {
                        String tmp = cur.getString(1);
                        if (tmp.equals("delete")) {
                            reference.child("/Service/" + cur.getInt(0) + "").removeValue();
                        } else {
                            Cursor t_cur = lite.rawQuery("Select * from Service where Id=?", new String[]{cur.getInt(0) + ""});
                            if (t_cur.getCount() > 0) {
                                while (t_cur.moveToNext()) {
                                    list.clear();
                                    int i = 0;
                                    while (i < t_cur.getColumnCount()) {
                                        if (i == 0) {
                                            list.put(t_cur.getColumnName(i), t_cur.getInt(i) + "");
                                        } else {
                                            list.put(t_cur.getColumnName(i), t_cur.getString(i));
                                        }
                                        i++;
                                    }
                                    reference.child("/Service/" + userid + "/" + cur.getInt(0) + "").setValue(list);
                                }
                            }
                            t_cur.close();
                        }
                    }
                    lite.delete("tmp_service", null, null);
                    cur.close();
                    c_service_tmp.close();
                }

                counter = 0;
                reference.child("/Service/").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        counter = dataSnapshot.getChildrenCount();
                        Log.d(" Firebase2", " Counted Services: " + counter);

                        int i = 0;

                        try {

                            ArrayList<Object> obj = (ArrayList<Object>) dataSnapshot.getValue();

                            if (obj != null) {

                                Log.w(" Values of Service ", obj.toString());
                                if(list!=null)
                                    list.clear();
                                while (i < obj.size()) {

                                    if (obj.get(i) != null) {

                                        list = (HashMap<String, String>) obj.get(i);


                                        Cursor cc = lite.rawQuery("Select Id from Service where Id=?", new String[]{list.get("Id")});

                                        ContentValues vals = new ContentValues();
                                        vals.put("Name", list.get("Name"));
                                        vals.put("ServiceType", list.get("ServiceType"));
                                        vals.put("Phone", list.get("Phone"));
                                        vals.put("DateCrt", list.get("DateCrt"));
                                        vals.put("Id", list.get("Id"));
                                        vals.put("City", list.get("City"));
                                        vals.put("Description", list.get("Description"));
                                        vals.put("Street", list.get("Street"));
                                        vals.put("Zipcode", list.get("Zipcode"));
                                        vals.put("State", list.get("State"));
                                        vals.put("Country", list.get("Country"));
                                        vals.put("Area", list.get("Area"));
                                        vals.put("UserId", list.get("UserId"));
                                        vals.put("ServiceProvider", list.get("ServiceProvider"));


                                        if (cc.getCount() == 0) {

                                            lite.insert("Service", null, vals);



                                        }else{
                                            lite.update("Service",vals,"Id=?",new String[]{list.get("Id")});
                                        }
                                        cc.close();
                                    }
                                    i++;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.w("userid", dataSnapshot.getKey());
                        }



                        if (counter == 0) {
                            Cursor t_cur = lite.rawQuery("Select * from Service", null);
                            if (t_cur.getCount() > 0) {
                                while (t_cur.moveToNext()) {
                                    i = 0;
                                    while (i < t_cur.getColumnCount()) {
                                        if (i == 0) {
                                            list.put(t_cur.getColumnName(i), t_cur.getInt(i) + "");
                                        } else {
                                            list.put(t_cur.getColumnName(i), t_cur.getString(i));
                                        }
                                        i++;
                                    }
                                    reference.child("/Service/" + t_cur.getInt(0) + "").setValue(list);
                                    list.clear();
                                }
                            }
                            t_cur.close();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Log.d(" Firebase", " Counted Services: " + counter);

            }
        }
    }

    public void syncSchedule(){
        if(checkConnection()) {

            if (lite != null) {

                lite.delete("tmp_schedule", "Id=? or Id=?", new String[]{"null",""});

                Cursor c_schedule_tmp = lite.rawQuery("Select Id from tmp_schedule where UserId=?", new String[]{userid});

                if (c_schedule_tmp.getCount() > 0) {
                    Cursor cur = lite.rawQuery("Select Id,change from tmp_schedule where UserId=?;", new String[]{userid});
                    while (cur.moveToNext()) {
                        String xx = cur.getInt(0) + "";
                        String tmp = cur.getString(cur.getColumnIndex("change"));
                        Log.w(" tmp_schedule", "Id: " + xx + " change: " + tmp);
                        if (tmp.equals("delete") || xx.equals("null")) {
                            Log.w(" tmp_schedule", "Id: " + xx + " change: " + tmp);
                            reference.child("/Schedule/" + userid + "/" + cur.getInt(0) + "").removeValue();
                        } else {
                            Cursor t_cur = lite.rawQuery("Select * from Schedule where id=?", new String[]{cur.getInt(0) + ""});
                            list = new HashMap<>();
                            if (t_cur.getCount() > 0) {
                                while (t_cur.moveToNext()) {
                                    list.clear();
                                    int i = 0;
                                    while (i < t_cur.getColumnCount()) {
                                        if (i == 0) {
                                            list.put(t_cur.getColumnName(i), t_cur.getInt(i) + "");
                                        } else {
                                            list.put(t_cur.getColumnName(i), t_cur.getString(i));
                                        }
                                        i++;
                                    }
                                    Log.w(" these is written", cur.getInt(0) + "");
                                    reference.child("/Schedule/" + userid + "/" + cur.getInt(0) + "").setValue(list);
                                }
                            }
                            t_cur.close();
                        }
                    }

                    lite.delete("tmp_schedule", null, null);
                    cur.close();
                    c_schedule_tmp.close();

                } else {

                    lite.delete("schedule", "id=?", new String[]{"null"});
                    Cursor t_cur = lite.rawQuery("Select * from Schedule", null);
                    list = new HashMap<>();
                    if (t_cur.getCount() > 0) {
                        while (t_cur.moveToNext()) {
                            int i = 0;
                            list.clear();
                            while (i < t_cur.getColumnCount()) {
                                if (i == 0) {
                                    list.put(t_cur.getColumnName(i), t_cur.getInt(i) + "");
                                } else {
                                    list.put(t_cur.getColumnName(i), t_cur.getString(i));
                                }
                                i++;
                            }
                            reference.child("/Schedule/" + userid + "/" + t_cur.getInt(0) + "").setValue(list);
                        }
                    }
                    t_cur.close();
                }

                reference.child("/Schedule").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String id = userid;
                        long c = dataSnapshot.child(id).getChildrenCount();
                        int i = 0;
                        while (i < c) {

                            list = (HashMap<String, String>) dataSnapshot.child(id + "/" + i).getValue();
                            if (list != null) {


                                Cursor cc = lite.rawQuery("Select Id from Schedule where Id=?", new String[]{list.get("Id")});

                                if (cc.getCount() == 0 && list.get("Id") != "null") {

                                    ContentValues vals = new ContentValues();
                                    vals.put("Id", list.get("Id"));
                                    vals.put("DateCrt", list.get("DateCrt"));
                                    vals.put("Place", list.get("Place"));
                                    vals.put("DateFrom", list.get("DateFrom"));
                                    vals.put("DateTo", list.get("DateTo"));
                                    vals.put("Description", list.get("Description"));
                                    vals.put("UserId", list.get("UserId"));
                                    Log.w(" Fire to sql", " Id: " + list.get("Id"));
                                    lite.insert("Schedule", null, vals);

                                }
                                cc.close();
                            }
                            i++;
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{ createDatabase();}
            reference.child("/Schedule/" + userid + "/" + "null" + "").removeValue();
        }


    }

    void syncSheduleAppoint(){
        if(checkConnection()){
            if(lite != null){
                lite.delete("SchedAppoint","Id = ? or Id=?",new String []{"null",""});
                reference.child("/SchedAppoint").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long cnt = dataSnapshot.getChildrenCount();

                        HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                                //Log.d(" ScheduleAppoint", " appoint" + map.toString());

                        if (cnt > 0) {



                            if (map != null) {

                                Iterator<String> it = map.keySet().iterator();

                                for (; it.hasNext(); ) {

                                    try {

                                        HashMap<String,Object> obj = (HashMap<String, Object>) map.get(it.next());

                                        if ( obj != null ) {

                                            Iterator<String> keyId = obj.keySet().iterator();

                                            for(;keyId.hasNext();){
                                                HashMap<String,String> sa = (HashMap<String, String>) obj.get(keyId.next());
                                                ContentValues val = new ContentValues();
                                                if(sa!=null){
                                                    val.clear();
                                                    val.put("Id", sa.get("Id"));
                                                    val.put("appointId", sa.get("appointId"));
                                                    val.put("clientId", sa.get("clientId"));
                                                    val.put("providerId", sa.get("providerId"));
                                                    Cursor cx = lite.rawQuery("Select * from schedappoint where Id=?", new String[]{sa.get("Id")});
                                                    if(cx.getCount()==0){
                                                        lite.insert("SchedAppoint", null, val);
                                                        Log.w("Error",val.toString());
                                                    }else{
                                                        lite.update("SchedAppoint", val, "Id=?",new String[]{val.getAsString("Id")});
                                                    }
                                                }
                                            }




                                        /*while(keyId.hasNext()) {

                                            ArrayList<Object> obx = (ArrayList<Object>) obj.get(keyId.next());
                                            int tmpx = 0;

                                            while (tmpx < obj.size()) {

                                                if (obx != null) {
                                                    Log.d(" log", obx.toString());
                                                    Cursor cx = lite.rawQuery("Select * from schedappoint where Id=?", new String[]{obx.get("Id")});
                                                    if (cx.getCount() == 0) {
                                                        ContentValues val = new ContentValues();
                                                        val.put("Id", obx.get("Id"));
                                                        val.put("appointId", obx.get("appointId"));
                                                        val.put("clientId", obx.get("clientId"));
                                                        val.put("providerId", obx.get("providerId"));
                                                        lite.insert("SchedAppoint", null, val);
                                                    }
                                                }
                                                tmpx++;
                                            }
                                        }
                                    }*/

                                        }

                                        Log.d(" log", obj.toString());
                                    }catch (Exception e){
                                        if(it.hasNext()) {
                                            ArrayList<Object> obj = (ArrayList<Object>) map.get(it.next());

                                            if (obj != null) {

                                                Iterator<Object> keyId = obj.iterator();

                                                for (; keyId.hasNext(); ) {
                                                    HashMap<String, String> sa = (HashMap<String, String>) keyId.next();
                                                    ContentValues val = new ContentValues();
                                                    if (sa != null) {
                                                        val.clear();
                                                        val.put("Id", sa.get("Id"));
                                                        val.put("appointId", sa.get("appointId"));
                                                        val.put("clientId", sa.get("clientId"));
                                                        val.put("providerId", sa.get("providerId"));
                                                        Cursor cx = lite.rawQuery("Select * from schedappoint where Id=?", new String[]{sa.get("Id")});
                                                        if (cx.getCount() == 0) {
                                                            lite.insert("SchedAppoint", null, val);
                                                            Log.w("Error", val.toString());
                                                        } else {
                                                            lite.update("SchedAppoint", val, "Id=?", new String[]{val.getAsString("Id")});
                                                        }
                                                    }
                                                }




                                        /*while(keyId.hasNext()) {

                                            ArrayList<Object> obx = (ArrayList<Object>) obj.get(keyId.next());
                                            int tmpx = 0;

                                            while (tmpx < obj.size()) {

                                                if (obx != null) {
                                                    Log.d(" log", obx.toString());
                                                    Cursor cx = lite.rawQuery("Select * from schedappoint where Id=?", new String[]{obx.get("Id")});
                                                    if (cx.getCount() == 0) {
                                                        ContentValues val = new ContentValues();
                                                        val.put("Id", obx.get("Id"));
                                                        val.put("appointId", obx.get("appointId"));
                                                        val.put("clientId", obx.get("clientId"));
                                                        val.put("providerId", obx.get("providerId"));
                                                        lite.insert("SchedAppoint", null, val);
                                                    }
                                                }
                                                tmpx++;
                                            }
                                        }
                                    }*/

                                            }
                                        }

                                    }



                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    SQLiteDatabase createDatabase() {
        try {
            if (lite == null) {

                lite = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH, null);

                //lite.execSQL("drop table appoint;");
                lite.execSQL("Create table if not exists Appoint (Id int,Name varchar(50),ServiceType varchar(30),ServiceProvider varchar(50),DateCrt varchar(10),ADate varchar(10),ATime varchar(10),Place varchar(30),Description varchar(255),UserId varchar(30),Status varchar(15));");
                //lite.execSQL("drop table Service;");
                lite.execSQL("Create table if not exists Service (Id int,Name varchar(50),ServiceProvider varchar(30), ServiceType varchar(30),Description varchar(255),UserId varchar(30),DateCrt varchar(10),Status varchar(15),Phone varchar(10),Street varchar(30),Area varchar(30),Place varchar(15),Zipcode varchar(6),city varchar(30),State varchar(30),Country varchar(30));");
                //lite.execSQL("drop table Schedule");
                lite.execSQL("Create table if not exists Schedule (Id int,Name varchar(50),DateFrom varchar(10),DateTo varchar(10),DateCrt varchar(10),UserId varchar(30),Place varchar(30),Description varchar(255));");
                lite.execSQL("Create table if not exists SchedAppoint (Id int,appointId int,ClientId varchar(30),providerId varchar(30))");
                lite.execSQL("Create table if not exists ServiceType (Id int,Name varchar(50));");
                lite.execSQL("Create table if not exists SchedAppoint (Id int,clientId varchar(30),providerId varchar(30),appointId int);");

                //lite.execSQL("drop table tmp_appoint");
                //lite.execSQL("drop table tmp_service");
                //lite.execSQL("drop table tmp_schedule");

                lite.execSQL("Create table if not exists tmp_appoint (Id int,change varchar(10),UserId varchar(30));");
                lite.execSQL("Create table if not exists tmp_service (Id int,change varchar(10),UserId varchar(30));");
                lite.execSQL("Create table if not exists tmp_schedule (Id int,change varchar(10),UserId varchar(30));");
                lite.execSQL("Create table if not exists tmp_scheduappoint (Id int,change varchar(10));");

                lite.execSQL("Create table if not exists User (Id int,Name varchar(30),email varchar(50),userid varchar(50));");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return lite;

    }

    boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected())
            return true;
        else
            return false;

    }

    void addAppointment(HashMap<String, String> data) {

        if ( checkConnection() ) {

            if(data.get("Id")!="null")
            reference.child("/Appointment/" + data.get("Id")).setValue(data);
            Log.w(" Adding "," Apponitment "+data);

        }

    }

    void removeAppointment(String id) {
        if (checkConnection()) {

            reference.child("/Appointment/" + id).removeValue();
            Log.w(" Removing"," Appointment "+id);

        }
    }

    public void addService(HashMap<String, String> map) {

        if (checkConnection()) {

            if(map.get("Id")!="null") {

                reference.child("/Service/" + map.get("Id")).setValue(map);
                Log.w(" Adding "," Service "+map);

            }
        }
    }

    void syncTempTables(){

    }

    void removeService(int id) {

        if (checkConnection()) {
            reference.child("/Service/" + id).removeValue();
            Log.w(" Removing"," Service"+id);
        }

    }

    void addUser(UserData data) {

        if (checkConnection()) {

            if (lite != null) {
                Cursor c = lite.rawQuery("Select count(*) from User", null);
                c.moveToFirst();
                int cnt = c.getInt(0);
                ContentValues val = new ContentValues();
                val.put("Id", cnt + 1);
                val.put("Name", data.getName());
                val.put("Email", data.getEmail());
                val.put("Userid", data.getUserId());
                lite.insert("user", null, val);
                reference.child("/User/" + data.getUserId()).setValue(data);
                c.close();

            }
        }

    }

    public void addScheduleAppoint(HashMap<String, String> m) {
        if(checkConnection()){
            if(m.get("Id")!="null")
            reference.child("/SchedAppoint/"+m.get("providerId")+"/"+m.get("Id")).setValue(m);
            Log.w("Adding"," Shadappoint"+m.toString());
        }
    }

    public void addSchedule(HashMap<String, String> map) {
        if(checkConnection()){
            if(!map.get("Id").equals("null"))
            reference.child("/Schedule/"+userid+"/"+map.get("Id")).setValue(map);
            Log.w("Adding"," Schedule "+map.toString());
        }
    }

    public void removeSchedule(String id) {
        if(checkConnection()){
            reference.child("/Schedule/"+userid+"/"+id).removeValue();
            Log.w(" Removing"," Schedule "+id.toString());
        }
    }

    public void removeSchedAppoint(String id) {
        if(checkConnection()){
            reference.child("/Schedule/"+id).removeValue();
            Log.w(" Removing"," ScheduleAppoint "+id.toString());
        }
    }

    public void clearSql() {

        Log.w(" Clean","Sqlite database");

        lite.execSQL("drop table appoint;");
        lite.execSQL("Create table if not exists Appoint (Id int,Name varchar(50),ServiceType varchar(30),ServiceProvider varchar(50),DateCrt varchar(10),ADate varchar(10),ATime varchar(10),Place varchar(30),Description varchar(255),UserId varchar(30),Status varchar(15));");
        lite.execSQL("drop table Service;");
        lite.execSQL("Create table if not exists Service (Id int,Name varchar(50),ServiceProvider varchar(30), ServiceType varchar(30),Description varchar(255),UserId varchar(30),DateCrt varchar(10),Status varchar(15),Phone varchar(10),Street varchar(30),Area varchar(30),Place varchar(15),Zipcode varchar(6),city varchar(30),State varchar(30),Country varchar(30));");
        lite.execSQL("drop table Schedule");
        lite.execSQL("Create table if not exists Schedule (Id int,Name varchar(50),DateFrom varchar(10),DateTo varchar(10),DateCrt varchar(10),UserId varchar(30),Place varchar(30),Description varchar(255));");
        lite.execSQL("drop table tmp_appoint");
        lite.execSQL("drop table tmp_service");
        lite.execSQL("drop table tmp_schedule");
        lite.execSQL("Create table if not exists tmp_appoint (Id int,change varchar(10),UserId varchar(30));");
        lite.execSQL("Create table if not exists tmp_service (Id int,change varchar(10),UserId varchar(30));");
        lite.execSQL("Create table if not exists tmp_schedule (Id int,change varchar(10),UserId varchar(30));");

        String paths[] = {"/Appointment/","/Schedule/"+userid,"/Service","/SchedAppoint"};
        if(checkConnection()){
            for(String x : paths){
                reference.child(x+"/").removeValue();
            }
        }
    }
}