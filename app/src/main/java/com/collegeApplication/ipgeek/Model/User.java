package com.collegeApplication.ipgeek.Model;

public class User {

    private String email,fullname,id,username,profileimageurl;

    public User(){

    }

    public User(String email, String fullname, String id, String username, String profileimageurl) {
        this.email = email;
        this.fullname = fullname;
        this.id = id;
        this.username = username;
        this.profileimageurl = profileimageurl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileimageurl() {
        return profileimageurl;
    }

    public void setProfileimageurl(String profileimageurl) {
        this.profileimageurl = profileimageurl;
    }
}
