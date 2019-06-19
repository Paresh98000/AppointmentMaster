package spksolutions.appointment.appointmentmaster;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.util.Log;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;



public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListner;

    private TextView tv_total_a_count,tv_total_s_count,tv_total_schedule,tv_username,tv_connect;
    private Snackbar connectionSnackbar;
    private ListView listViewA,listViewSer,listViewSch,listViewSchApp;

    public static boolean userIsAvailabel,appointmentfetched,isConnected,firebase_ui_ishown;

    private String username,sharedUserFileName="user";

    private int apt_count,apt_clicked;

    private SharedPreferences mSharedPreferences;
    SharedPreferences.Editor editor;
    private Thread setUserThread;
    private Thread checkconnectionThread;
    private String s1,userid;
    private ArrayList<HashMap<String, String>> appointlist,schedulelist,servicelist;
    private boolean servicefetched;
    private String service_count;
    private boolean schedulefetched,schedappfetched;
    private int sch_count;
    private int ser_count;

    ServiceArrayAdapter adapter;
    ScheduleArrayAdapter adapter_sch;
    AppointArrayAdapter adapter_appoint;

    char selectedTab;

    SQLiteDatabase sqlite;
    private FirebaseSupport fs;
    private int schapp_count;

    static boolean connection_changing;

    static Thread connectionThread;

    static ConnectivityManager cm;
    private String useremail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apt_clicked = -1;

        //fs.addAppointment(new AppointmentData());

        username  = null;

        fs = new FirebaseSupport(getBaseContext(),this);

        sqlite = fs.createDatabase();

        apt_count = 0;

        mSharedPreferences = getSharedPreferences(sharedUserFileName,MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        editor.apply();

        userIsAvailabel = false;

        firebase_ui_ishown = false;

        setUserThread = checkconnectionThread = null;

        tv_total_a_count = findViewById(R.id.txt_count_total_appointment);
        tv_total_s_count = findViewById(R.id.txt_total_service_count);
        tv_total_schedule = findViewById(R.id.txt_total_schedules);
        tv_username = findViewById(R.id.txt_username);
        tv_connect = findViewById(R.id.tv_connection);

        listViewA = findViewById(R.id.lst_view_appointment);
        listViewSer = findViewById(R.id.lst_view_service);
        listViewSch = findViewById(R.id.lst_view_schedule);
        listViewSchApp = findViewById(R.id.lst_view_appointments_in_sch);

        tv_username.setText("No user available");
        tv_username.setTextColor(Color.RED);
        tv_connect.setVisibility(View.VISIBLE);
        tv_connect.setTextColor(Color.MAGENTA);

        userid = getSharedDataPreferenceString("userId");

        Log.w(" Userid",userid+" if no user");

        selectedTab = 'A';

        listViewA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                apt_clicked=position;
            }
        });

        listViewA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getBaseContext(),EditAppointmentActivity.class);
                i.putExtra("Clicked",adapter_appoint.getIndex(position));
                startActivityForResult(i,100);
            }
        });

        listViewSer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getBaseContext(),EditService.class);
                i.putExtra("Clicked",adapter.getId(position));
                i.putExtra("Provider",adapter.getProviderId(position));
                startActivityForResult(i,400);
            }
        });

        listViewSch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getBaseContext(),EditScheduleActivity.class);
                i.putExtra("Clicked",adapter_sch.getId(position));
                startActivityForResult(i,500);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_EXTERNAL_STORAGE},500);
        }else{

        }
        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
            sqlite = SQLiteDatabase.openOrCreateDatabase(ConstantFTP.Database.DB_PATH,null);
            Log.w(" DB"," Database created " + sqlite.toString());
        }

        checkConnection();
        onUserReady();

        mAuthStateListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){

                    // user sign in
                    UserData data = new UserData(user);
                    addUser(data);
                    username = user.getDisplayName();
                    userIsAvailabel = true;
                    firebase_ui_ishown=false;
                    Log.d(" Here ","Auth is called");
                    setDataToSharedPreference("userId",user.getUid());
                    setDataToSharedPreference("userName",user.getDisplayName());
                    setDataToSharedPreference("email", user.getEmail());
                    userid=user.getUid();
                    if(setUserThread!=null);
                    setUserThread.interrupt();
                    onUserReady();

                } else {

                    // user sign out
                    userIsAvailabel = false;
                    username = "No user found";
                    userid="No data found";
                    useremail = "no data found";
                    setDataToSharedPreference("UserAvailable","False");
                    setDataToSharedPreference("userName","No User");
                    setDataToSharedPreference("userId","No User");
                    setDataToSharedPreference("email","No User");

                    if(!firebase_ui_ishown) {
                        List<AuthUI.IdpConfig> providers = Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                        );
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(providers)
                                        .build(), 1500
                        );

                        firebase_ui_ishown=true;

                    }


                }

            }
        };

        fs.cleanTables();

        fs.syncServiceType();
/*
        Calendar date_a = Calendar.getInstance();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP,date_a.getTimeInMillis(),null);

        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", cal.getTimeInMillis());
        intent.putExtra("allDay", false);
        intent.putExtra("rrule", "FREQ=DAILY");
        intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
        intent.putExtra("title", "A Test Event from android app");
        startActivity(intent);

        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, date_a.getTimeInMillis(), sender);*/

        CalendarSupport cs = new CalendarSupport(getBaseContext());

        //cs.createReminder("Hellow","Hi","Bhavnagar",System.currentTimeMillis(),System.currentTimeMillis());
        //cs.updateReminder("10","Radhey Radhey","Please go to sleep now","Rajkot Hostel", System.currentTimeMillis());

    }

    private void addUser(UserData data) {
        fs.addUser(data);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mFirebaseAuth!=null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListner);
        }
        if(checkconnectionThread!=null && checkconnectionThread.isAlive()){
            checkconnectionThread.interrupt();
        }
        if(setUserThread !=null && setUserThread.isAlive()){
            setUserThread.interrupt();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(mFirebaseAuth!=null){
            mFirebaseAuth.addAuthStateListener(mAuthStateListner);
        }

        if(selectedTab=='A'){
            try {
                syncAppoints();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(selectedTab=='S'){
            try {
                syncServices();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                syncSchedule();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1500){
            if(resultCode == RESULT_OK){
                Toast.makeText(this,"Signed In",Toast.LENGTH_SHORT).show();
            } else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this,"Sign In Canceled",Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if(selectedTab=='A'){
            try {
                syncAppoints();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(selectedTab=='S'){
            try {
                syncServices();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                syncSchedule();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void setDataToSharedPreference(String key, String value){
        editor.putString(key,value);
        editor.apply();
    }

    public String getSharedDataPreferenceString(String key){
        return mSharedPreferences.getString(key,null);
    }

    public void onNewAppointmentClicked(View v){

        Intent addnewap = new Intent(this,AddAppointmentActivity.class);
        startActivity(addnewap);

    }

    public void onNewServiceClicked(View v){

        Intent addnewap = new Intent(this,AddServiceActivity.class);
        startActivity(addnewap);
    }

    public void onNewScheduleClicked(View v){

        Intent addnewap = new Intent(this,AddScheduleActivity.class);
        startActivity(addnewap);

    }

    public void onLogOutButtonPressed(View v){
        if(fs.checkConnection()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Are you sure do you want to Logout ?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mFirebaseAuth.signOut();
                    tv_username.setText("No User selected");
                    tv_username.setTextColor(Color.RED);
                }
            });

            alert.setNegativeButton("No", null);

            alert.create().show();
        }else{
            Toast.makeText(getBaseContext(),"Sorry Try Later, You are Offline",Toast.LENGTH_LONG).show();

        }
    }

    public void onAppointmentTabClicked(View v){
        selectedTab='A';
        findViewById(R.id.frag_appoinm).setVisibility(View.VISIBLE);
        findViewById(R.id.frag_service).setVisibility(View.GONE);
        findViewById(R.id.frag_schedule).setVisibility(View.GONE);
        findViewById(R.id.fab_appoint_valu).setVisibility(View.VISIBLE);
        findViewById(R.id.fab_schedule_valu).setVisibility(View.GONE);
        findViewById(R.id.fab_service_valu).setVisibility(View.GONE);
        findViewById(R.id.linear_apoint).setBackgroundResource(R.drawable.border_1_white_back);
        findViewById(R.id.linear_serv).setBackgroundResource(R.drawable.border1);
        findViewById(R.id.linear_schedule).setBackgroundResource(R.drawable.border1);
        try {
            syncAppoints();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    void syncAppoints() throws Exception{

        fs.syncAppointments();

        sqlite.delete("Appoint","Id=? or Id=?",new String[]{"","null"});
        Cursor c = sqlite.rawQuery("Select * from appoint where UserId=? Order by Id desc",new String[]{userid});
        ArrayList<HashMap<String,String>> listAppoint = new ArrayList<>();
        if(c.getCount()>0){
            while(c.moveToNext()){
                int i=0;
                HashMap<String,String> ap = new HashMap<>();
                while(i<c.getColumnCount()){
                    if(i==0){
                        ap.put(c.getColumnName(i),c.getInt(i)+"");
                    }else{
                        ap.put(c.getColumnName(i),c.getString(i));
                    }
                    i++;
                }
                listAppoint.add(ap);
            }
        }
        Log.w(" DataA",listAppoint.toString());
        adapter_appoint = new AppointArrayAdapter(this,R.layout.appointment_item,listAppoint);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listViewA.setAdapter(adapter_appoint);
            }
        });


        apt_count = listAppoint.size();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_total_a_count.setText(apt_count+" Appointments");
            }
        });

        appointmentfetched = true;

        c.close();
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return super.supportShouldUpRecreateTask(targetIntent);
    }

    public void onServiceTabClicked(View v){
        selectedTab = 'S';
        findViewById(R.id.frag_appoinm).setVisibility(View.GONE);
        findViewById(R.id.frag_service).setVisibility(View.VISIBLE);
        findViewById(R.id.frag_schedule).setVisibility(View.GONE);
        findViewById(R.id.fab_appoint_valu).setVisibility(View.GONE);
        findViewById(R.id.fab_schedule_valu).setVisibility(View.GONE);
        findViewById(R.id.fab_service_valu).setVisibility(View.VISIBLE);
        findViewById(R.id.linear_apoint).setBackgroundResource(R.drawable.border1);
        findViewById(R.id.linear_serv).setBackgroundResource(R.drawable.border_1_white_back);
        findViewById(R.id.linear_schedule).setBackgroundResource(R.drawable.border1);

        try {
            syncServices();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void syncServices() throws Exception{

        fs.syncService();

        //sqlite.delete("Service","Id=? or Id=? or UserId=? or UserId=?",new String[]{"","null","null",""});

        Cursor c = sqlite.rawQuery("Select * from Service Order by Id desc",null);

        ArrayList<HashMap<String, String>> listService = new ArrayList<HashMap<String, String>>();

        if(c.getCount()>0){
            while(c.moveToNext()){
                int i=0;
                HashMap<String,String> ap = new HashMap<>();
                while(i<c.getColumnCount()){
                    if(i==0){
                        ap.put(c.getColumnName(i),c.getInt(i)+"");
                    }else{
                        ap.put(c.getColumnName(i),c.getString(i));
                    }
                    i++;
                }
                listService.add(ap);
            }
        }

        Log.w(" DataS",listService.toString());
        adapter = new ServiceArrayAdapter(this,R.layout.service_item,listService);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listViewSer.setAdapter(adapter);
            }
        });

        ser_count = listService.size();
        servicefetched = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_total_s_count.setText(ser_count+" Service");
            }
        });
        c.close();
    }

    public void onScheduleTabClicked(View v){
        selectedTab='H';
        findViewById(R.id.frag_appoinm).setVisibility(View.GONE);
        findViewById(R.id.frag_service).setVisibility(View.GONE);
        findViewById(R.id.frag_schedule).setVisibility(View.VISIBLE);
        findViewById(R.id.fab_appoint_valu).setVisibility(View.GONE);
        findViewById(R.id.fab_schedule_valu).setVisibility(View.VISIBLE);
        findViewById(R.id.fab_service_valu).setVisibility(View.GONE);
        findViewById(R.id.linear_apoint).setBackgroundResource(R.drawable.border1);
        findViewById(R.id.linear_serv).setBackgroundResource(R.drawable.border1);
        findViewById(R.id.linear_schedule).setBackgroundResource(R.drawable.border_1_white_back);
        try {

            syncSchedule();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void syncSchedule() throws Exception{

        fs.syncSchedule();
        fs.syncSheduleAppoint();

        Cursor c = sqlite.rawQuery("Select * from Schedule where UserId=? Order by Id desc",new String[]{userid});
        ArrayList<HashMap<String, String>> listAppoint = new ArrayList<>();
        if(c.getCount()>0){
            while(c.moveToNext()){
                int i=0;
                HashMap<String,String> ap = new HashMap<>();
                while(i<c.getColumnCount()){
                    if(i==0){
                        ap.put(c.getColumnName(i),c.getInt(i)+"");
                    }else{
                        ap.put(c.getColumnName(i),c.getString(i));
                    }
                    i++;
                }
                listAppoint.add(ap);
            }
        }

        Log.w(" DataSch",listAppoint.toString());
        adapter_sch = new ScheduleArrayAdapter(this,R.layout.schedule_item,listAppoint);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listViewSch.setAdapter(adapter_sch);
            }
        });


        sch_count = listAppoint.size();
        schedulefetched = true;

        sqlite.delete("SchedAppoint","Id=? or Id=?",new String[]{"","null"});
        c = sqlite.rawQuery("Select * from SchedAppoint Where providerId=? Order by Id desc",new String[]{userid});
        Log.d(" userid",userid+" this is id");
        listAppoint = new ArrayList<HashMap<String, String>>();
        if(c.getCount()>0){
            while(c.moveToNext()){
                Cursor shadapp = sqlite.rawQuery("Select * from Appoint where Id=? and userId=? limit 1;",new String[]{c.getInt(c.getColumnIndex("appointId"))+"",userid});
                if (shadapp.getCount() > 0) {
                    while(shadapp.moveToNext()) {
                        int i=0;
                        HashMap<String,String> ap = new HashMap<>();
                        while(i < shadapp.getColumnCount()) {
                            if (i == 0) {
                                ap.put(shadapp.getColumnName(i), shadapp.getInt(i) + "");
                            } else {
                                ap.put(shadapp.getColumnName(i), shadapp.getString(i));
                            }
                            i++;
                        }
                        ap.put("providerId",c.getString(c.getColumnIndex("providerId")));
                        ap.put("SchedAppointId",c.getInt(0)+"");
                        listAppoint.add(ap);
                    }
                }
                shadapp.close();
            }
        }
        c.close();

        Log.w(" DataScheAppoint",listAppoint.toString());
        final SchedAppArrayAdapter sacadapter = new SchedAppArrayAdapter(this,R.layout.schedappoint_item,listAppoint);
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        listViewSchApp.setAdapter(sacadapter);
                    }
                }
        );


        schapp_count = listAppoint.size();
        sch_count += schapp_count;
        //tv_total_schedule.setText(sch_count+" Schedule");
        schedappfetched = true;

        sqlite.delete("Schedule","Id=? or Id=?",new String[]{"","null"});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add("White");
        //menu.add("Magenta");
        menu.add("CheckConnection");
        menu.add("SynchroniseData");
        menu.add("Cleardata");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getTitle().equals("White")){

            findViewById(R.id.constraintview).setBackgroundColor(Color.WHITE);

        }else if(item.getTitle().equals("CheckConnection")){

            if(checkconnectionThread!=null&&checkconnectionThread.isAlive()){
                //Toast.makeText(getBaseContext(),"Try Again for connection",Toast.LENGTH_SHORT).show();
                checkconnectionThread.interrupt();
            }else{
                try {
                    checkConnection();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            fs.syncAppointments();
            fs.syncServiceType();
            fs.syncService();
            fs.syncSchedule();
            if(setUserThread!=null&&setUserThread.isAlive()){
                //Toast.makeText(getBaseContext(),"Try Again for connection",Toast.LENGTH_SHORT).show();
                setUserThread.interrupt();
            }else {
                try{
                    setUserThread.interrupt();
                    onUserReady();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        } else
        if(item.getTitle().equals("Cleardata")){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Are you sure do you want to Delete everything");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fs.clearSql();
                    onAppointmentTabClicked(null);
                }
            });
            alert.setNegativeButton("No",new DialogInterface.OnClickListener(){
               @Override
               public void onClick(DialogInterface dialog, int which){

               }
            });
            alert.setTitle("Clear All Data");
            alert.create().show();
        }

        if(item.getTitle()=="SynchronizeData"){
            fs.syncSchedule();
            fs.syncServiceType();
            fs.syncAppointments();
            fs.syncService();
            onUserReady();
        }

        return true;
    }

    public final void onUserReady(){

        if(setUserThread==null || setUserThread.getState() == Thread.State.TERMINATED) {
            setUserThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    final String s = "User not connected";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_total_a_count.setText(s);
                            tv_total_s_count.setText(s);
                            tv_total_schedule.setText(s);
                        }
                    });

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(mFirebaseAuth==null)
                                mFirebaseAuth = FirebaseAuth.getInstance();

                            int i = 0;

                            while(i<3 && !userIsAvailabel){
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                i++;
                            }

                            if(userIsAvailabel) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_username.setText(username);
                                        tv_username.setTextColor(Color.BLACK);
                                    }
                                });
                            }
                        }
                    }).start();

                    fs.createDatabase();

                    setAppointLists();
                    setServiceLists();
                    setScheduleLists();

                    final String str = "Fetching Data";
                    while (!appointmentfetched) {
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_a_count.setText(str);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_a_count.setText(str + ".");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_a_count.setText(str + "..");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_a_count.setText(str + "...");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        if (ni == null) {
                            break;
                        }

                        if( ni!=null && ni.isConnected() == false){
                            break;
                        }

                        if (userIsAvailabel == false) {
                            break;

                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_total_a_count.setText(apt_count+" Appointment");
                            tv_total_s_count.setText("Fetching Data");
                            tv_total_schedule.setText("Fetching data");

                        }
                    });

                    while (!servicefetched) {
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_s_count.setText(str);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_s_count.setText(str + ".");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                tv_total_s_count.setText(str + "..");

                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                tv_total_s_count.setText(str + "...");

                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        assert cm != null;
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        if (ni == null) {
                            break;
                        }

                        if( ni!=null && !ni.isConnected()){
                            break;
                        }

                        if (!userIsAvailabel) {
                            break;

                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_total_s_count.setText(ser_count+" Service");
                            tv_total_schedule.setText("Fetching data");

                        }
                    });

                    while (!schedulefetched) {
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                tv_total_schedule.setText(str);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {tv_total_schedule.setText(str + ".");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {tv_total_schedule.setText(str + "..");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {tv_total_schedule.setText(str + "...");
                            }
                        });
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("Text View", "Error in thread line 80 Main Activity.java");
                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        if (ni == null) {
                            break;
                        }

                        if( ni!=null && ni.isConnected() == false){
                            break;
                        }

                        if (userIsAvailabel == false) {
                            break;

                        }
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_total_schedule.setText(sch_count+" Schedule");

                        }
                    });

                }
            });

            setUserThread.start();

        }else{
            if(setUserThread.isAlive()){
                setUserThread.interrupt();
            }

        }
    }

    public void setAuth(){

        if(mFirebaseAuth==null){mFirebaseAuth = FirebaseAuth.getInstance();}
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user!=null){


            fs = new FirebaseSupport(getBaseContext(),this);
            userIsAvailabel=true;
            username=user.getDisplayName();
            setDataToSharedPreference("previousUserId",user.getUid());
            setDataToSharedPreference("email", user.getEmail());
            setDataToSharedPreference("userAvailable","true");

        }else
        {
            userIsAvailabel=true;
            username="No User found";
            setDataToSharedPreference("UserAvailable","false");
            setDataToSharedPreference("email", "No email");
        }
    }

    public final void checkConnection(){


        //Toast t = Toast.makeText(getBaseContext(),"Checking Connection",Toast.LENGTH_LONG);
        //t.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
        //t.show();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if( networkInfo != null && networkInfo.isConnected() ){

            isConnected = true;

            if(connectionSnackbar!=null && connectionSnackbar.isShown()) {
                connectionSnackbar.dismiss();
            }

            mFirebaseAuth = FirebaseAuth.getInstance();

            setAuth();

            if(checkconnectionThread==null || checkconnectionThread.getState() == Thread.State.TERMINATED) {

                checkconnectionThread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_connect.setVisibility(View.VISIBLE);
                                tv_connect.setTextColor(Color.MAGENTA);
                            }
                        });

                        s1 = "Checking Connection";
                        int cnt = 1;
                        while(cnt <= 3) {
                            if(Thread.currentThread().isInterrupted()){
                                break;
                            }
                            if (firebase_ui_ishown) {
                                break;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {

                            }
                            if(Thread.currentThread().isInterrupted()){
                                break;
                            }
                            s1 = s1 + ".";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_connect.setText(s1);
                                }
                            });
                            if(Thread.currentThread().isInterrupted()){
                                break;
                            }
                            cnt += 1;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_connect.setVisibility(View.GONE);
                            }
                        });

                    }
                });
                checkconnectionThread.start();
            }else {
                if (checkconnectionThread.isAlive()) {
                    checkconnectionThread.interrupt();
                }
            }

        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(connectionSnackbar==null){
                        connectionSnackbar = Snackbar.make(findViewById(R.id.constraintview), "OFFLINE MODE (Options > Checkconnection)", Snackbar.LENGTH_LONG);
                    }
                    if(!connectionSnackbar.isShown()) {
                        connectionSnackbar.show();
                    }
                    tv_connect.setVisibility(View.VISIBLE);
                    tv_connect.setText("No Connection");
                    tv_connect.setTextColor(Color.RED);
                }
            });

        }

    }

    void setAppointLists(){

        try {
            syncAppoints();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void setServiceLists(){

        try {
            syncServices();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void setScheduleLists(){
        try {
            syncSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
