package de.dennisadam.eva.server.chat;

import de.dennisadam.eva.server.user.User;

import java.sql.Timestamp;

public class Message {

    private final User sender;
    private final Timestamp timestamp;
    private final String message;

    public Message(User sender, String message) {
        this.sender = sender;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.message = message;
    }

    public String getMessage(){
        return "[" + timestamp + "] " + sender.getUsername() + ": " + message;
    }

    public User getSender() {
        return sender;
    }
}
