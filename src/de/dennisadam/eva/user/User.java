package de.dennisadam.eva.user;

import de.dennisadam.eva.server.Server;

public class User {
    private int userID;
    private String username;
    private boolean isOnline = false;

    //Konstruktor für neue Benutzer, die erst eine ID zugewiesen bekommen
    public User(String username) {
        this.userID = Server.userList.size();
        this.username = username;
        this.isOnline = true;
    }

    public User(int userID, String username) {
        this.userID = userID;
        this.username = username;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
