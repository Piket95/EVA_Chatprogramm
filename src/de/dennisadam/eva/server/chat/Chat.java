package de.dennisadam.eva.server.chat;

import de.dennisadam.eva.server.user.User;
import de.dennisadam.eva.server.user.UserStatus;

import java.io.PrintWriter;
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
                    MEMBER[1].getWriter().println();
                    MEMBER[1].getWriter().println("Du hast eine neue Nachricht im Chat mit \"" + MEMBER[0].getUsername() + "\" erhalten!");
                    MEMBER[1].getWriter().println("Gib /chat " + MEMBER[0].getUsername() + " ein, um den Chat zu betreten und die Nachricht zu lesen!");

                    MEMBER[0].getWriter().flush();
                    MEMBER[1].getWriter().flush();
                }
            }
            else{
                if( (MEMBER[0].getStatus() == UserStatus.ONLINE) && (this.userChatStatus0 == ChatStatus.LEFT) ){
                    MEMBER[1].getWriter().println("[" + message.getTimestamp() + "] " + message.getSender().getUsername() + ": " + message.getMessage());
                    MEMBER[0].getWriter().println();
                    MEMBER[0].getWriter().println("Du hast eine neue Nachricht von " + MEMBER[1].getUsername() + " erhalten!");
                    MEMBER[0].getWriter().println("Gib /chat " + MEMBER[1].getUsername() + " ein, um den Chat zu betreten und die Nachricht zu lesen!");

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

    public void getArchiv(PrintWriter writer){

        writer.println();
        writer.println("\"------------Letzten 10 Nachrichten dieser Konversation-----------\"");

        if(oldMessages.size() == 0){
            writer.println("Archiv ist leer! Möglicherweise hat vorher noch kein Nachrichtenaustausch stattgefunden!");
        }
        else{
            for(Messages messages : oldMessages){
                writer.println("[" + messages.getTimestamp() + "] " + messages.getSender().getUsername() + ": " + messages.getMessage());
            }
        }
        writer.println("------------------------------------------------------------------");
        writer.flush();

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

    public void getCommandList(PrintWriter writer){

        writer.println();
        writer.println("----------------------Liste der Chat-Befehle---------------------");
        writer.println("/help\t\t\t\t\t\tZeigt die Liste der verfügbaren Befehle (diese hier)");
        writer.println("/archiv\t\t\t\t\t\tZeigt die letzten 10 Nachrichten an");
        writer.println("/leave\t\t\t\t\t\tVerlasse den aktuellen Chat");
        writer.println("-----------------------------------------------------------------");
        writer.flush();

    }

    //TODO: Testen
    public void joinChat(User joinedUser){
        if(joinedUser == MEMBER[0]){
            userChatStatus0 = ChatStatus.JOINED;

            if(userChatStatus1 == ChatStatus.JOINED){
                MEMBER[1].getWriter().println();
                MEMBER[1].getWriter().println(MEMBER[0].getUsername() + " hat den Chat betreten!");
                MEMBER[1].getWriter().flush();
            }
        }
        else{
            userChatStatus1 = ChatStatus.JOINED;

            if(userChatStatus0 == ChatStatus.JOINED){
                MEMBER[0].getWriter().println();
                MEMBER[0].getWriter().println(MEMBER[1].getUsername() + " hat den Chat betreten!");
                MEMBER[0].getWriter().flush();
            }
        }
    }

    //TODO: Testen
    public void leaveChat(User leavingUser){
        if(MEMBER[0] == leavingUser){
            userChatStatus0 = ChatStatus.LEFT;
            MEMBER[1].getWriter().println();
            MEMBER[1].getWriter().println(MEMBER[0].getUsername() + " hat den Chat verlassen!");
            MEMBER[1].getWriter().flush();
        }
        else {
            userChatStatus1 = ChatStatus.LEFT;
            MEMBER[0].getWriter().println();
            MEMBER[0].getWriter().println(MEMBER[1].getUsername() + " hat den Chat betreten!");
            MEMBER[0].getWriter().flush();
        }
    }

    public void userDisconnectError(User currentUser){
        leaveChat(currentUser);
        if(MEMBER[0] == currentUser){
            MEMBER[1].getWriter().println();
            MEMBER[1].getWriter().println(MEMBER[0].getUsername() + " hat die Verbindung zum Server verloren!");
            MEMBER[1].getWriter().flush();
        }
        else {
            userChatStatus1 = ChatStatus.LEFT;
            MEMBER[0].getWriter().println();
            MEMBER[0].getWriter().println(MEMBER[1].getUsername() + " hat die Verbindung zum Server verloren!");
            MEMBER[0].getWriter().flush();
        }
    }
}
