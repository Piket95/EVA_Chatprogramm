package de.dennisadam.eva.server.user;

import java.util.*;

public class UserList {

    private final List<User> users;

    public UserList(List<User> users) {
        this.users = users;
    }

    public User getUserFromList(String username){
        for(User u : users){
            if(u.getUsername().equals(username)){
                return u;
            }
        }
        return null;
    }

    public void add(User u){
        users.add(u);
    }

    public List<User> getUserList() {
        return users;
    }
}
