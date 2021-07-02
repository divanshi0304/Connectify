package com.example.connectify.Models;

public class Users {

    private String name, email, phone, image, cover, uid, password, onlineStatus;

    public Users() {
    }

    public Users(String name, String email, String phone, String image, String cover, String uid, String search, String onlineStatus) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
