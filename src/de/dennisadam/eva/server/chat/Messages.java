package de.dennisadam.eva.server.chat;

import de.dennisadam.eva.server.user.User;

import java.sql.Timestamp;

public class Messages {

    private User sender;
    private Timestamp timestamp;
    private String message;

    public Messages(User sender, String message) {
        this.sender = sender;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
