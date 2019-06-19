package spksolutions.appointment.appointmentmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class FirebaseSupport {

    Context c;

    public static FirebaseSupport instance;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseAuth mFirebaseAuth;

    SQLiteDatabase mSQLiteDatabase;

    boolean checknameexists;

    static long total_appointment,total_users,total_services,total_schedules,counts,total_s_types;

    ArrayList<AppointmentData> mAppointmentDataList;
    ArrayList<ScheduleData> mServiceDataList;
    ArrayList<ScheduleData> mScheduleDataList;
    ArrayList<UserData> mUserDataList;

    private String userid;


    private HashMap<String,Long> IDs;

    static final String APPOINT_PATH = "/Appointment";
    static final String SERVICE_PATH = "/Service";
    static final String SCHEDULE_PATH = "/Schedule";
    static final String USER_PATH = "/User";
    public static String[] mServiceTypes;
    static boolean DATA_RECEIVED[];
    static int INDEX_DATA_R;

    static long[] COUNTER_VAR;
    static String STRING_VAR[];
    final static int TOTAL_FLAGS = 50;
    static AppointmentData APPOINTDATA_VAR;
    static ServiceData SERVICEDATA_VAR;
    static ScheduleData SCHEDULEDATA_VAR;
    static UserData USERDATA_VAR;

    static FirebaseSupport getInstance(){
        return instance;
    }

    FirebaseSupport(){

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("/");
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAppointmentDataList = new ArrayList<>();
        mScheduleDataList = new ArrayList<>();
        mServiceDataList = new ArrayList<>();

        DATA_RECEIVED = new boolean[TOTAL_FLAGS];
        COUNTER_VAR = new long[TOTAL_FLAGS];
        STRING_VAR = new String[TOTAL_FLAGS];
        INDEX_DATA_R = 0;

        if(mFirebaseAuth!=null){
            userid = mFirebaseAuth.getUid();
        }
        else{
            userid = "";
        }

        setIds();

        instance = this;

        DATA_RECEIVED[INDEX_DATA_R] = true;
    }

    public void createDatabase(String userId){
        if(mSQLiteDatabase == null) {

            mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(userId, null);
            //mSQLiteDatabase.execSQL("Drop table Appoint;");
            mSQLiteDatabase.execSQL("Create table if not exists Appoint (Id int,Name varchar(50),ServiceType varchar(30),ServiceProvider varchar(50),DateCrt varchar(10),ADate varchar(10),ATime varchar(10),Place varchar(30),Description varchar(255),Status varchar(15));");
            //mSQLiteDatabase.execSQL("drop table Service;");
            mSQLiteDatabase.execSQL("Create table if not exists Service (Id int,Name varchar(50),ServiceProvider varchar(30), ServiceType varchar(30),Description varchar(255),DateCrt varchar(10),Phone varchar(10),Street varchar(30),Area varchar(30),Place varchar(15),Zipcode varchar(6),city varchar(30),State varchar(30),Country varchar(30));");
            //mSQLiteDatabase.execSQL("drop table Schedule");

            mSQLiteDatabase.execSQL("Create table if not exists Schedule (Id int,Name varchar(50),DateFrom varchar(10),DateTo varchar(10),DateCrt varchar(10),Place varchar(30),Description varchar(255));");
            mSQLiteDatabase.execSQL("Create table if not exists ServiceType (Id int,Name varchar(50));");
            mSQLiteDatabase.execSQL("Create table if not exists tmp_appoint (Id int);");
            mSQLiteDatabase.execSQL("Create table if not exists tmp_service (Id int);");
            mSQLiteDatabase.execSQL("Create table if not exists tmp_schedule (Id int);");
        }
    }

    void increment_DataReceiveIndex(){
        if(INDEX_DATA_R<TOTAL_FLAGS-1) {
            INDEX_DATA_R += 1;
        }else{
            INDEX_DATA_R = 0;
        }
    }

    public int getStringFrom(final String path,final String child){

            final int cur = INDEX_DATA_R;
            DATA_RECEIVED[cur] = false;
        increment_DataReceiveIndex();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Path",path);
                mDatabaseReference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        STRING_VAR[cur] = dataSnapshot.child(child).getValue(String.class);
                        Log.d("STR",STRING_VAR[cur]);
                        DATA_RECEIVED[cur] = true;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).start();

        return cur;
    }

    public int getCount(final String path) {
        final int cur = INDEX_DATA_R;
        DATA_RECEIVED[cur] = false;
        increment_DataReceiveIndex();
        mDatabaseReference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                COUNTER_VAR[cur] = dataSnapshot.getChildrenCount();
                DATA_RECEIVED[cur] = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return cur;
    }

    public long getCountUser(){
        return total_users;
    }

    public boolean setIds() {

        setIdforAppointment();

        setIdforService();

        setIdforSchedule();


        setIdforUser();

        return true;
    }

    void setIdforUser() {new Thread(new Runnable() {
        @Override
        public void run() {
            int a = getCount(USER_PATH);

            while(!DATA_RECEIVED[a]) {
            }

            total_users = COUNTER_VAR[a];
            mDatabaseReference.child("/ID/U").setValue(total_users);
        }
    }).start();
    }

    void setIdforSchedule() {new Thread(new Runnable() {
        @Override
        public void run() {
            int a = getCount(SCHEDULE_PATH+"/"+userid);

            while(!DATA_RECEIVED[a]) {
            }

            total_schedules = COUNTER_VAR[a];
            mDatabaseReference.child("/ID/"+userid+"/Sch").setValue(total_schedules);
        }
    }).start();
    }

    void setIdforService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int a = getCount(SERVICE_PATH+userid);

                while(!DATA_RECEIVED[a]) {
                }

                total_services = COUNTER_VAR[a];
                mDatabaseReference.child("/ID/"+userid+"/Ser").setValue(total_services);
            }
        }).start();
    }

    void setIdforAppointment() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int a = getCount(APPOINT_PATH+"/"+userid);

                while(!DATA_RECEIVED[a]) {
                }

                total_appointment = COUNTER_VAR[a];
                mDatabaseReference.child("/ID/"+userid+"/A").setValue(total_appointment);

            }
        }).start();
    }

    void addUser(UserData data){
        data.setAppointments(getAppointmentsByUser());
        data.setID((total_users)+"");
        mDatabaseReference.child("/User/"+data.getUserId()).setValue(data);
        setIdforUser();
    }

    void addAppointment(final AppointmentData data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int a = getCount(APPOINT_PATH+"/"+userid);

                while(!DATA_RECEIVED[a]) {

                }

                total_appointment = COUNTER_VAR[a];
                mDatabaseReference.child("/ID/"+userid+"/A").setValue(total_appointment);
                data.setAid(String.valueOf(total_appointment));
                mDatabaseReference.child("/Appointment/"+data.getUid()+"/"+total_appointment).setValue(data.generateData());
            }
        }).start();

        setIdforAppointment();
    }

    void addService(ServiceData data){
        mDatabaseReference.child("/Service/"+total_services).setValue(data);
        setIdforService();
    }

    void addSchedule(ScheduleData data){
        total_schedules+=1;
        mDatabaseReference.child("/Schedule/"+total_schedules).setValue(data);
        setIdforSchedule();
    }

    public ArrayList<AppointmentData> get_A_Data(final String user_id){

        mAppointmentDataList.clear();

        mDatabaseReference.child("/Appointment/"+user_id+"/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                counts = dataSnapshot.getChildrenCount();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    mAppointmentDataList.add(ds.getValue(AppointmentData.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.w(" AppointmentData",user_id+" "+mAppointmentDataList.toString());
        return  mAppointmentDataList;
    }

    public int get_A_Data(final boolean connection) {

        final int cur = INDEX_DATA_R;
        DATA_RECEIVED[cur] = false;
        increment_DataReceiveIndex();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSQLiteDatabase == null)
                    createDatabase("data/data/" + c.getPackageName() + "/db");
                if (connection) {
                    mDatabaseReference.child("/Appointment/" + userid + "/").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            COUNTER_VAR[cur] = dataSnapshot.getChildrenCount();
                            int i = 1;
                            boolean isempty = false;

                            Cursor c = mSQLiteDatabase.rawQuery("Select count(*) as \"c\" from Appoint", null);
                            c.moveToFirst();
                            int t = c.getInt(0);

                            Log.w("Total in database", t + " cursor count " + c.getCount());

                            if (t > 0) {
                                isempty = false;
                            } else {
                                isempty = true;
                            }

                            while (i <= COUNTER_VAR[cur]) {

                                HashMap<String, String> d = (HashMap<String, String>) dataSnapshot.child(i + "").getValue();

                                if (isempty) {
                                    ContentValues vals = new ContentValues();
                                    vals.put("Id", d.get("Id"));
                                    vals.put("ServiceProvider", d.get("ServiceProvider"));
                                    vals.put("ServiceType", d.get("ServiceType"));
                                    vals.put("DateCrt", d.get("DateCrt"));
                                    vals.put("ADate", d.get("ADate"));
                                    vals.put("ATime", d.get("ATime"));
                                    vals.put("Place", d.get("Place"));
                                    vals.put("Description", d.get("Description"));
                                    vals.put("Status", d.get("Status"));
                                    vals.put("Name", d.get("Name"));

                                    mSQLiteDatabase.insert("Appoint", null, vals);
                                } else {
                                    Cursor cur = mSQLiteDatabase.rawQuery("Select Id from Appoint where Id=?", new String[]{d.get("Id")});
                                    if (cur.getCount() == 0) {
                                        ContentValues vals = new ContentValues();
                                        vals.put("Id", d.get("Id"));
                                        vals.put("ServiceProvider", d.get("ServiceProvider"));
                                        vals.put("ServiceType", d.get("ServiceType"));
                                        vals.put("DateCrt", d.get("DateCrt"));
                                        vals.put("ADate", d.get("ADate"));
                                        vals.put("ATime", d.get("ATime"));
                                        vals.put("Place", d.get("Place"));
                                        vals.put("Description", d.get("Description"));
                                        vals.put("Status", d.get("Status"));
                                        vals.put("Name", d.get("Name"));
                                        mSQLiteDatabase.insert("Appoint", null, vals);
                                    }
                                }
                                //Toast.makeText(c.getApplicationContext(), "Total inserted appointments " + x, Toast.LENGTH_LONG).show();
                                i += 1;
                            }
                            //Log.w("Appoints in Fsu",mAppointmentDataList.get(0).getName()+" Counts "+i);
                            DATA_RECEIVED[cur] = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    try {
                        Cursor cur = mSQLiteDatabase.rawQuery("Select distinct Id from tmp_appoint", null);
                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {
                                HashMap<String, String> appoint = new HashMap<>();
                                Cursor cur_a = mSQLiteDatabase.rawQuery("Select * from Appoint where Id=?", new String[]{cur.getInt(0) + ""});
                                if (cur_a.getCount() > 0) {
                                    while (cur_a.moveToNext()) {
                                        int i = cur_a.getColumnCount();
                                        while (i > 0) {
                                            if (i == 1) {
                                                appoint.put(cur_a.getColumnName(i - 1), cur_a.getInt(i - 1) + "");
                                            } else {
                                                appoint.put(cur_a.getColumnName(i - 1), cur_a.getString(i - 1));
                                            }
                                            i--;
                                        }
                                    }
                                }
                                mDatabaseReference.child("/Appointment/" + userid + "/" + cur.getInt(0)).setValue(appoint);
                                mSQLiteDatabase.delete("tmp_appoint", "Id=?", new String[]{cur.getInt(0) + ""});
                            }
                        }
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    }
                }else{
                    DATA_RECEIVED[cur]=true;
                }
            }


        }).start();


        return cur;
    }

    ArrayList<AppointmentData> getAppointmentListReady(){
        return mAppointmentDataList;
    }

    String getCurrenctUserId(){
        String str = mFirebaseAuth.getUid();
        if(str!="" || str != null) {
            return mFirebaseAuth.getUid();
        }else{
            return "No user found";
        }
    }

    public boolean checkAppointmentNameExists(String name){

        checknameexists = false;
        //.orderByChild("userId").equalTo(getCurrenctUserId())
        mDatabaseReference.child("/Appointment/"+userid).orderByChild("name").equalTo(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0){
                    checknameexists = true;
                    Log.w("Data",dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return checknameexists;
    }

    public int getServicesTypes(){

        final int flag = INDEX_DATA_R;
        DATA_RECEIVED[flag] = false;
        increment_DataReceiveIndex();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.w("Total","Ser_T"+total_s_types);

                int x = countServicesTypes();

                while(!DATA_RECEIVED[x]){}

                Log.d("ERROR n",total_s_types+"");
                if(total_s_types>0){

                    mServiceTypes = new String[(int) total_s_types];
                    //Log.d("Size of type_S",total_s_types+"");
                    int c = 1;

                    while(c<=total_s_types){

                        int xyz = getStringFrom("/ServiceType",""+c);

                        while(!DATA_RECEIVED[xyz]){}

                        //Log.d("C:"+c,"Str:"+STRING_VAR[xyz]);

                        mServiceTypes[c-1] = STRING_VAR[xyz];
                        ContentValues vals = new ContentValues();
                        vals.put("Id",""+c);
                        vals.put("Name",STRING_VAR[xyz]);
                        mSQLiteDatabase.insert("ServiceType",null,vals);
                        c += 1;
                    }
                    //Log.d("STREND",mServiceTypes[0]+mServiceTypes[1]);
                    DATA_RECEIVED[flag] = true;

                }
            }
        }).start();

        return flag;
    }

    public int countServicesTypes(){

        final int x = INDEX_DATA_R;
        DATA_RECEIVED[x] = false;
        increment_DataReceiveIndex();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = getCount("/ServiceType");

                while(!DATA_RECEIVED[i]){

                }

                total_s_types = COUNTER_VAR[i];
                //Log.d("ERROR b",total_s_types+"");
                DATA_RECEIVED[x] = true;
            }
        }).start();

        return x;
    }

    public long getAppointmentsByUser(){
        mDatabaseReference.child("/Appointment/"+userid+"/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                counts = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return counts;
    }

    public long getAppointmentsByUser(String user_id){
        mDatabaseReference.child("/Appointment/"+user_id+"/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                counts = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return counts;
    }

    LinkedList<String> getDataStringList(String path,final int from,final int to){
        final LinkedList<String> data = new LinkedList<>();

        mDatabaseReference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int from1 = from;
                while(from1<=to){
                    data.add(dataSnapshot.child(""+from1).getValue(String.class));
                    from1++;
                }
                Log.w("Now LL","Let see it in "+data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.w("Now LL","Let see it out "+data);
        return data;
    }

    public int get_Sch_Data(final boolean connection) {

        final int cur = INDEX_DATA_R;
        DATA_RECEIVED[cur] = false;
        increment_DataReceiveIndex();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSQLiteDatabase == null)
                    createDatabase("data/data/" + c.getPackageName() + "/db");
                if (connection) {
                    mDatabaseReference.child("/Schedule/" + userid + "/").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            COUNTER_VAR[cur] = dataSnapshot.getChildrenCount();
                            int i = 1;
                            boolean isempty = false;

                            Cursor c = mSQLiteDatabase.rawQuery("Select count(*) as \"c\" from Schedule", null);
                            c.moveToFirst();
                            int t = c.getInt(0);

                            Log.w("Total in database", t + " cursor count " + c.getCount());

                            if (t > 0) {
                                isempty = false;
                            } else {
                                isempty = true;
                            }

                            while (i <= COUNTER_VAR[cur]) {

                                HashMap<String, String> d = (HashMap<String, String>) dataSnapshot.child(i + "").getValue();

                                if (isempty) {
                                    ContentValues vals = new ContentValues();

                                    vals.put("Id", d.get("Id"));
                                    vals.put("DateCrt", d.get("DateCrt"));
                                    vals.put("Description", d.get("Description"));
                                    vals.put("Name", d.get("Name"));
                                    vals.put("DateFrom", d.get("DateFrom"));
                                    vals.put("DateTo", d.get("DateTo"));
                                    vals.put("Place", d.get("Place"));

                                    mSQLiteDatabase.insert("Schedule", null, vals);
                                } else {
                                    Cursor cur = mSQLiteDatabase.rawQuery("Select Id from Appoint where Id=?", new String[]{d.get("Id")});
                                    if (cur.getCount() == 0) {

                                        ContentValues vals = new ContentValues();

                                        vals.put("Id", d.get("Id"));
                                        vals.put("DateCrt", d.get("DateCrt"));
                                        vals.put("Description", d.get("Description"));
                                        vals.put("Name", d.get("Name"));
                                        vals.put("DateFrom", d.get("DateFrom"));
                                        vals.put("DateTo", d.get("DateTo"));
                                        vals.put("Place", d.get("Place"));

                                        mSQLiteDatabase.insert("Schedule", null, vals);
                                    }
                                }
                                //Toast.makeText(c.getApplicationContext(), "Total inserted appointments " + x, Toast.LENGTH_LONG).show();
                                i += 1;
                            }
                            //Log.w("Appoints in Fsu",mAppointmentDataList.get(0).getName()+" Counts "+i);
                            DATA_RECEIVED[cur] = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    try {
                        Cursor cur = mSQLiteDatabase.rawQuery("Select distinct Id from tmp_schedule", null);
                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {
                                HashMap<String, String> appoint = new HashMap<>();
                                Cursor cur_a = mSQLiteDatabase.rawQuery("Select * from schedule where Id=?", new String[]{cur.getInt(0) + ""});
                                if (cur_a.getCount() > 0) {
                                    while (cur_a.moveToNext()) {
                                        int i = cur_a.getColumnCount();
                                        while (i > 0) {
                                            if (i == 1) {
                                                appoint.put(cur_a.getColumnName(i - 1), cur_a.getInt(i - 1) + "");
                                            } else {
                                                appoint.put(cur_a.getColumnName(i - 1), cur_a.getString(i - 1));
                                            }
                                            i--;
                                        }
                                    }
                                }
                                mDatabaseReference.child("/Schedule/" + userid + "/" + cur.getInt(0)).setValue(appoint);
                                mSQLiteDatabase.delete("tmp_schedule", "Id=?", new String[]{cur.getInt(0) + ""});
                            }
                        }
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    }
                }else{
                    DATA_RECEIVED[cur] = true;
                }
            }

        }).start();


        return cur;
    }

    public int get_Ser_Data(final boolean connection) {

        final int cur = INDEX_DATA_R;
        DATA_RECEIVED[cur] = false;
        increment_DataReceiveIndex();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSQLiteDatabase == null)
                    createDatabase("data/data/" + c.getPackageName() + "/db");
                if (connection) {
                    mDatabaseReference.child("/Service/" + userid + "/").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            COUNTER_VAR[cur] = dataSnapshot.getChildrenCount();
                            int i = 1;
                            boolean isempty = false;

                            Cursor c = mSQLiteDatabase.rawQuery("Select count(*) as \"c\" from Service", null);
                            c.moveToFirst();
                            int t = c.getInt(0);

                            Log.w("Total in database", t + " cursor count " + c.getCount());

                            if (t > 0) {
                                isempty = false;
                            } else {
                                isempty = true;
                            }

                            while (i <= COUNTER_VAR[cur]) {

                                HashMap<String, String> d = (HashMap<String, String>) dataSnapshot.child(i + "").getValue();

                                if (isempty) {
                                    ContentValues vals = new ContentValues();
                                    vals.put("Id", d.get("Id"));
                                    vals.put("ServiceProvider", d.get("ServiceProvider"));
                                    vals.put("ServiceType", d.get("ServiceType"));
                                    vals.put("DateCrt", d.get("DateCrt"));
                                    vals.put("Phone", d.get("Phone"));
                                    vals.put("Street", d.get("Street"));
                                    vals.put("Area", d.get("Area"));
                                    vals.put("Description", d.get("Description"));
                                    vals.put("Status", d.get("Status"));
                                    vals.put("Name", d.get("Name"));
                                    vals.put("Zipcode", d.get("Zipcode"));
                                    vals.put("City", d.get("City"));
                                    vals.put("State", d.get("State"));
                                    vals.put("Country", d.get("Country"));

                                    mSQLiteDatabase.insert("Service", null, vals);
                                } else {
                                    Cursor cur = mSQLiteDatabase.rawQuery("Select Id from Appoint where Id=?", new String[]{d.get("Id")});
                                    if (cur.getCount() == 0) {
                                        ContentValues vals = new ContentValues();
                                        vals.put("Id", d.get("Id"));
                                        vals.put("ServiceProvider", d.get("ServiceProvider"));
                                        vals.put("ServiceType", d.get("ServiceType"));
                                        vals.put("DateCrt", d.get("DateCrt"));
                                        vals.put("Phone", d.get("Phone"));
                                        vals.put("Street", d.get("Street"));
                                        vals.put("Area", d.get("Area"));
                                        vals.put("Description", d.get("Description"));
                                        vals.put("Status", d.get("Status"));
                                        vals.put("Name", d.get("Name"));
                                        vals.put("Zipcode", d.get("Zipcode"));
                                        vals.put("City", d.get("City"));
                                        vals.put("State", d.get("State"));
                                        vals.put("Country", d.get("Country"));
                                        mSQLiteDatabase.insert("Service", null, vals);
                                    }
                                }
                                //Toast.makeText(c.getApplicationContext(), "Total inserted appointments " + x, Toast.LENGTH_LONG).show();
                                i += 1;
                            }
                            //Log.w("Appoints in Fsu",mAppointmentDataList.get(0).getName()+" Counts "+i);
                            DATA_RECEIVED[cur] = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    try {
                        Cursor cur = mSQLiteDatabase.rawQuery("Select distinct Id from tmp_service", null);
                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {
                                HashMap<String, String> appoint = new HashMap<>();
                                Cursor cur_a = mSQLiteDatabase.rawQuery("Select * from Service where Id=?", new String[]{cur.getInt(0) + ""});
                                if (cur_a.getCount() > 0) {
                                    while (cur_a.moveToNext()) {
                                        int i = cur_a.getColumnCount();
                                        while (i > 0) {
                                            if (i == 1) {
                                                appoint.put(cur_a.getColumnName(i - 1), cur_a.getInt(i - 1) + "");
                                            } else {
                                                appoint.put(cur_a.getColumnName(i - 1), cur_a.getString(i - 1));
                                            }
                                            i--;
                                        }
                                    }
                                }
                                mDatabaseReference.child("/Service/" + userid + "/" + cur.getInt(0)).setValue(appoint);
                                mSQLiteDatabase.delete("tmp_service", "Id=?", new String[]{cur.getInt(0) + ""});
                            }
                        }
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    }
                }else{
                    DATA_RECEIVED[cur] = true;
                }
            }

        }).start();


        return cur;
    }
}
