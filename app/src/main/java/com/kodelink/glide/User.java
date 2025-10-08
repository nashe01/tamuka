package com.kodelink.glide;

public class User {
    public String name;
    public String phone;
    public String email;
    public String role;
    public String userId;

    public User() {
    }

    public User(String name, String phone, String email, String role) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.userId = generateUserId(phone, role);
    }

    private String generateUserId(String phone, String role) {
        return role + "_" + phone.replaceAll("[^0-9]", "");
    }
}







