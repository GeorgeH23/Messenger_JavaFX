package com.chatApplication.common;

public class NewUser {

    private String username;
    private String password;
    private String phone;
    private String picture;

    public NewUser(String username, String password, String phone, String picture) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.picture = picture;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getPicture() {
        return picture;
    }

    @Override
    public String toString() {
        return username + ", " + phone;
    }
}
