package spksolutions.appointment.appointmentmaster;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserData {

    private String userId;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String ID;

    public Long getAppointments() {
        return appointments;
    }

    public void setAppointments(Long appointments) {
        this.appointments = appointments;
    }

    private Long appointments;

    UserData(FirebaseUser mFirebaseUser){

        if(mFirebaseUser != null){
            userId = mFirebaseUser.getUid();
            name = mFirebaseUser.getDisplayName();
            email = mFirebaseUser.getEmail();
            phone = mFirebaseUser.getPhoneNumber();
        }
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
