package spksolutions.appointment.appointmentmaster;

import android.content.ContentValues;

import com.firebase.ui.auth.data.model.User;

import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.ZipEntry;

public class ScheduleData {

    private String DateCrt;
    private String Id;
    private String Name;

    public String getDateFrom() {
        return DateFrom;
    }

    public void setDateFrom(String dateFrom) {
        DateFrom = dateFrom;
    }

    public String getDateTo() {
        return DateTo;
    }

    public void setDateTo(String dateTo) {
        DateTo = dateTo;
    }

    private String Description;
    private String Place;
    private String DateFrom;
    private String DateTo;

    ScheduleData(){

        DateCrt=Id=Name=Description=Place=DateCrt="No Data Found";

    }


    public String getDateCrt() {
        return DateCrt;
    }

    public void setDateCrt(String dateCrt) {
        DateCrt = dateCrt;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        if(name=="")
            name="No name";
        Name = name;
    }

    String Userid;
    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPlace() {
        return Place;
    }

    public void setPlace(String place) {
        Place = place;
    }


    HashMap<String,String> getMap(){

        HashMap<String,String> m = new HashMap<>();
        m.put("Id",Id);
        m.put("Name",Name);
        m.put("DateCrt",DateCrt);
        m.put("DateFrom",DateFrom);
        m.put("Place",Place);
        m.put("DateTo",DateTo);
        m.put("Description",Description);
        m.put("UserId",Userid);


        return  m;
    }

    ContentValues getContentValue(){

        ContentValues val = new ContentValues();

        val.put("Id",Id);
        val.put("Name",Name);
        val.put("DateCrt",DateCrt);
        val.put("DateFrom",DateFrom);
        val.put("Place",Place);
        val.put("DateTo",DateTo);
        val.put("Description",Description);
        val.put("UserId",Userid);

        return val;

    }

    public void setUserId(String userid) {
        Userid = userid;
    }
}
