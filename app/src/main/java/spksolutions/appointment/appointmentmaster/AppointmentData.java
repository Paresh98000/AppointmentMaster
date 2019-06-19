package spksolutions.appointment.appointmentmaster;

import android.content.ContentValues;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AppointmentData {
    private String name;
    private String date_s;

    public String getDate_c_s() {
        return date_c_s;
    }

    public void setDate_c_s(String date_c_s) {
        this.date_c_s = date_c_s;
    }

    private String date_c_s;
    private String userId;
    private String time_s;
    private String service;
    private String s_provider;
    private String place;
    private String desc;
    private boolean apporoved;
    private Calendar date_a,date_c;
    private String aid;
    private int totalAppoint;
    public static int total_appointments;
    private HashMap<String,String> maindata;

    private String status;

    AppointmentData(){

        total_appointments = 0;
        name = date_s = time_s = service = s_provider = place = desc = userId = "no_data_found";
        date_c = Calendar.getInstance();
        date_a = null;
        status = "Not Approved";
        maindata = new HashMap<>();
    }

    AppointmentData(String uid,String n,String dt,String time,String serv, String provider,String place,String des){

        this.userId = uid;
        name = n;
        date_s = dt;
        time_s = time;
        service = serv;
        s_provider = provider;
        this.place = place;
        desc = des;

        try {

            Date d = new SimpleDateFormat().parse(dt);

            int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(d));
            int month = Integer.parseInt(new SimpleDateFormat("mm").format(d));
            int day = Integer.parseInt(new SimpleDateFormat("dd").format(d));

            d = new SimpleDateFormat().parse(time);

            int hour = Integer.parseInt(new SimpleDateFormat("HH").format(d));
            int minute = Integer.parseInt(new SimpleDateFormat("MM").format(d));

            date_a = Calendar.getInstance();
            date_a.set(year,month,day,hour,minute);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public HashMap<String, String> getMaindata() {
        return maindata;
    }

    public void setMaindata(HashMap<String, String> maindata) {
        this.maindata = maindata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HashMap<String,String> generateData(){

        maindata.put("Id",aid);
        maindata.put("ServiceProvider",s_provider);
        maindata.put("ServiceType",service);
        maindata.put("ADate",date_s);
        maindata.put("DateCrt",date_c_s);
        maindata.put("ATime",time_s);
        maindata.put("Place",place);
        maindata.put("Description",desc);
        maindata.put("Status",status);
        maindata.put("Name",name);
        maindata.put("UserId",userId);

        return maindata;
    }

    public ContentValues getGenerateContantValues(){
        ContentValues vals = new ContentValues();

        vals.put("Id",aid);
        vals.put("ServiceProvider",s_provider);
        vals.put("ServiceType",service);
        vals.put("DateCrt",date_c_s);
        vals.put("ADate",date_s);
        vals.put("ATime",time_s);
        vals.put("Place",place);
        vals.put("UserId",userId);
        vals.put("Description",desc);
        vals.put("Status",status);
        vals.put("Name",name);

        return vals;
    }

    public static int getTotal_appointments() {
        return total_appointments;
    }

    public static void setTotal_appointments(int total_appointments) {
        AppointmentData.total_appointments = total_appointments;
    }

    public String getUid() {
        return userId;
    }

    public void setUid(String uid) {
        this.userId = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_s() {
        return date_s;
    }

    public void setDate_s(String date_s) {
        this.date_s = date_s;
    }

    public String getTime_s() {
        return time_s;
    }

    public void setTime_s(String time_s) {
        this.time_s = time_s;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getS_provider() {
        return s_provider;
    }

    public void setS_provider(String s_provider) {
        this.s_provider = s_provider;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isApporoved() {
        return apporoved;
    }

    public void setApporoved(boolean apporoved) {
        this.apporoved = apporoved;
    }

    public Calendar getDate_a() {
        return date_a;
    }

    public void setDate_a(Calendar date_a) {

        this.date_a = date_a;

        date_s = new SimpleDateFormat("dd/MM/yyyy").format(date_a.getTime());
        time_s = new SimpleDateFormat("HH:mm").format(date_a.getTime());

    }

    public Calendar getDate_c() {
        return date_c;
    }

    public void setDate_c(Calendar date_c) {
        this.date_c = date_c;
        date_c_s = new SimpleDateFormat("dd/MM/yyyy").format(date_c.getTime());
    }

    public String getATimestamp(){
        return String.valueOf(date_a.getTimeInMillis());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
