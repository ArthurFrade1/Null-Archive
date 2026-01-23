package com.arthurfrade.nullarchive.dto;

public class UserSessionData {
    public final int id;
    public final String username;
    public final String role;

    public UserSessionData(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
