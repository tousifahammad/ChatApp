package com.bibhas.handsapp.Models;

/**
 * Created by Ankita on 20-02-2018.
 */

public class UserProfile {
    public String userAge;
    public String userEmail;
    public String userPassword;

    public UserProfile(){
    }

    public UserProfile(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}