package de.dennisadam.eva.server.chat;

import de.dennisadam.eva.server.user.User;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChatMember {

    private final User user;
    private ChatStatus status;

    private final List<Message> newMessages;

    public ChatMember(User user) {
        this.user = user;
        this.status = ChatStatus.LEFT;

        this.newMessages = new ArrayList<>();
    }

    public synchronized void printNewMessages(Chat currentChat){
        PrintWriter writer = this.getUser().getWriter();

        if(newMessages.size() != 0){
            writer.println();
            writer.println("-----------------Neue Nachrichten in Abwesenheit-----------------");
            Iterator<Message> iter = newMessages.iterator();
            Message message;
            while(iter.hasNext()){
                message = iter.next();
                writer.println(message.getMessage());

                currentChat.addArchiveMessage(message);
                currentChat.checkArchiveSize();
                iter.remove();
            }
            writer.println("-----------------------------------------------------------------");
            writer.flush();
        }
    }

    public synchronized int getNewMessagesSize(){
        return newMessages.size();
    }

    public synchronized void addNewMessage(Message message){
        newMessages.add(message);
    }

    public User getUser() {
        return user;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public ChatStatus getStatus() {
        return status;
    }
}
