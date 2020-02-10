package de.dennisadam.eva.chat;

import de.dennisadam.eva.user.User;
import de.dennisadam.eva.user.UserStatus;

import java.util.ArrayList;
import java.util.List;

public class Chat {

    private final User[] MEMBER;
    private final int ARCHIVE_SIZE = 10;

    private ChatStatus userChatStatus0;
    private ChatStatus userChatStatus1;

    private List<Messages> newMessages;
    private List<Messages> oldMessages;

    public Chat(User user1, User user2) {
        this.MEMBER = new User[]{user1, user2};
        this.newMessages = new ArrayList<>();
        this.oldMessages = new ArrayList<>();

        this.userChatStatus0 = ChatStatus.LEFT;
        this.userChatStatus1 = ChatStatus.LEFT;
    }

    public synchronized void sendMessage(Messages message){
        if( (userChatStatus0 == ChatStatus.JOINED) && (userChatStatus1 == ChatStatus.JOINED) ){
            MEMBER[0].getWriter().println("[" + message.getTimestamp() + "] " + message.getSender().getUsername() + ": " + message.getMessage());
            MEMBER[1].getWriter().println("[" + message.getTimestamp() + "] " + message.getSender().getUsername() + ": " + message.getMessage());

            oldMessages.add(message);
            checkArchiveSize();

            MEMBER[0].getWriter().flush();
            MEMBER[1].getWriter().flush();
        }
        else{
            newMessages.add(message);

            if(message.getSender() == MEMBER[0]){
                if( (MEMBER[1].getStatus() == UserStatus.ONLINE) && (this.userChatStatus1 == ChatStatus.LEFT) ){
                    MEMBER[0].getWriter().println("[" + message.getTimestamp() + "] " + message.getSender().getUsername() + ": " + message.getMessage());
                    MEMBER[1].getWriter().println("\nSie haben eine neue Nachricht im Chat mit \"" + MEMBER[0].getUsername() + "\" erhalten!");
                    MEMBER[1].getWriter().println("Geben Sie /chat " + MEMBER[0].getUsername() + " ein, um den Chat zu betreten und die Nachricht zu lesen!");

                    MEMBER[0].getWriter().flush();
                    MEMBER[1].getWriter().flush();
                }
            }
            else{
                if( (MEMBER[0].getStatus() == UserStatus.ONLINE) && (this.userChatStatus0 == ChatStatus.LEFT) ){
                    MEMBER[1].getWriter().println("[" + message.getTimestamp() + "] " + message.getSender().getUsername() + ": " + message.getMessage());
                    MEMBER[0].getWriter().println("Sie haben eine neue Nachricht von " + MEMBER[1].getUsername() + " erhalten!");
                    MEMBER[0].getWriter().println("Geben Sie /chat " + MEMBER[1].getUsername() + " ein, um den Chat zu betreten und die Nachricht zu lesen!");

                    MEMBER[0].getWriter().flush();
                    MEMBER[1].getWriter().flush();
                }
            }
        }
    }

    //Wenn mehr als 10 Nachrichten im Archiv hinterlegt sind, wird die 1. gelöscht, damit insgesamt nur 10 Nachrichten in der Liste sind
    public void checkArchiveSize(){
        if (oldMessages.size() > ARCHIVE_SIZE) {
            oldMessages.remove(0);
        }
    }

    public String getArchiv(){

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"------------Letzten 10 Nachrichten dieser Konversation-----------\"\n");

        if(oldMessages.size() == 0){
            stringBuilder.append("Archiv ist leer! Möglicherweise hat vorher noch kein Nachrichtenaustausch stattgefunden!\n");
        }
        else{
            for(Messages messages : oldMessages){
                stringBuilder.append("[" + messages.getTimestamp() + "] " + messages.getSender().getUsername() + ": " + messages.getMessage() + "\n");
            }
        }
        stringBuilder.append("------------------------------------------------------------------");

        return stringBuilder.toString();
    }

    public User[] getMEMBER() {
        return MEMBER;
    }

    public List<Messages> getNewMessages() {
        return newMessages;
    }

    public int countNewMessages(User me){
        int counter = 0;

        for(Messages messages : newMessages){
            if(messages.getSender() != me){
                counter++;
            }
        }

        return counter;
    }

    public String getCommandList(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("----------------------Liste der Chat-Befehle---------------------\n");
        stringBuilder.append("/help\t\t\t\t\t\tZeigt die Liste der verfügbaren Befehle (diese hier)\n");
        stringBuilder.append("/archiv\t\t\t\t\t\tZeigt die letzten 10 Nachrichten an\n");
        stringBuilder.append("/leave\t\t\t\t\t\tVerlasse den aktuellen Chat");

        return stringBuilder.toString();
    }

    public ChatStatus getUserChatStatus0() {
        return userChatStatus0;
    }

    public void setUserChatStatus0(ChatStatus userChatStatus0) {
        this.userChatStatus0 = userChatStatus0;
    }

    public ChatStatus getUserChatStatus1() {
        return userChatStatus1;
    }

    public void setUserChatStatus1(ChatStatus userChatStatus1) {
        this.userChatStatus1 = userChatStatus1;
    }
}
