package spksolutions.appointment.appointmentmaster;

import android.content.ContentValues;

import java.util.Calendar;
import java.util.HashMap;

public class ServiceData {

    private String Id;

    private String UserId;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    private String Status;

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getServiceProviderId() {
        return ServiceProviderId;
    }

    public void setServiceProviderId(String serviceProviderId) {
        ServiceProviderId = serviceProviderId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDateCrt() {
        return DateCrt;
    }

    public void setDateCrt(String dateCrt) {
        DateCrt = dateCrt;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getStreet() {
        return Street;
    }

    public void setStreet(String street) {
        Street = street;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getZipcode() {
        return Zipcode;
    }

    public void setZipcode(String zipcode) {
        Zipcode = zipcode;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    private String Name;
    private String Type;
    private String ServiceProviderId;
    private String Description;
    private String DateCrt;
    private String Phone;
    private String Street;
    private String Area;
    private String Zipcode;
    private String City;
    private String State;
    private String Country;

    public ServiceData(String id, String name, String serviceProviderId, String description, String dateCrt, String phone, String street, String area, String zipcode, String city, String state, String country) {
        Id = id;
        Name = name;
        ServiceProviderId = serviceProviderId;
        Description = description;
        DateCrt = dateCrt;
        Phone = phone;
        Street = street;
        Area = area;
        Zipcode = zipcode;
        City = city;
        State = state;
        Country = country;
    }

    public ServiceData(){
        Id = Name = ServiceProviderId=Description=DateCrt=Phone=Street=Area=Zipcode=City=State=Country = "No Data";
    }

    public ServiceData(String Name,String ProviderId,String Desc,String Type,String Phone,String City){

        super();
        this.Name = Name;
        this.City = City;
        this.Description = Desc;
        this.ServiceProviderId = ProviderId;
        this.Type = Type;
        this.Phone = Phone;
        Calendar c = Calendar.getInstance();

        this.DateCrt = c.get(Calendar.DATE)+"/"+c.get(Calendar.MONTH)+"/"+c.get(Calendar.YEAR);

    }

    HashMap<String,String> getMap(){

        HashMap<String,String> map = new HashMap<>();
        map.put("Name",Name);
        map.put("ServiceType",Type);
        map.put("Phone",Phone);
        map.put("DateCrt", DateCrt);
        map.put("Id",Id);
        map.put("UserId",UserId);
        map.put("Description",Description);
        map.put("Street",Street);
        map.put("Zipcode",Zipcode);
        map.put("State",State);
        map.put("Country",Country);
        map.put("Area",Area);
        map.put("City",City);
        map.put("ServiceProvider",ServiceProviderId);
        //map.put("ProviderName",providername);
        return map;

    }

    ContentValues getContentValue(){
        ContentValues val = new ContentValues();

        val.put("Name",Name);
        val.put("ServiceType",Type);
        val.put("Phone",Phone);
        val.put("DateCrt", DateCrt);
        val.put("Id",Id);
        val.put("City",City);
        val.put("Description",Description);
        val.put("Street",Street);
        val.put("Zipcode",Zipcode);
        val.put("State",State);
        val.put("Country",Country);
        val.put("Area",Area);
        val.put("UserId",UserId);
        val.put("ServiceProvider",ServiceProviderId);


        return val;
    }

    public void setId(String id){
        Id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userid) {
        UserId = userid;
    }
}
