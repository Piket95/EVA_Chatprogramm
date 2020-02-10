package de.dennisadam.eva.user;

import de.dennisadam.eva.chat.Chat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    //TODO: Passwort hinzufügen (mit Hashing)
    private UserStatus status;

    private PrintWriter writer;
    private BufferedReader reader;

    private List<Chat> chatliste;

    public User(String username, PrintWriter writer, BufferedReader reader) {
        this.username = username;
        this.status = UserStatus.ONLINE;
        this.reader = reader;
        this.writer = writer;

        this.chatliste = new ArrayList<>();
    }

    public List<Chat> getChatliste() {
        return chatliste;
    }

    public Chat chatExists(final User[] MEMBER){
        for(Chat chat : this.chatliste){
            if( ( (chat.getMEMBER()[0] == MEMBER[0]) && (chat.getMEMBER()[1] == MEMBER[1]) ) || ( (chat.getMEMBER()[0] == MEMBER[1]) && (chat.getMEMBER()[1] == MEMBER[0]) ) ){
                return chat;
            }
        }

        return null;
    }

    public void showChatList(){
        //TODO: Anzeige ob Chatpartner auch gerade online ist
        writer.println("Liste der aktiven Chats: ");

        if(this.getChatliste().size() != 0){
            for(Chat chat : this.getChatliste()){
                String preset;

                if(chat.getMEMBER()[0] != this){
                    preset = "- " + chat.getMEMBER()[0].getUsername();
                }
                else{
                    preset = "- " + chat.getMEMBER()[1].getUsername();
                }

                int newMessages = chat.countNewMessages(this);
                if(newMessages > 1){
                    writer.println(preset + " (" + newMessages + " neue Nachrichten)");
                }
                else if(newMessages == 1){
                    writer.println(preset + " (" + newMessages + " neue Nachricht)");
                }
                else{
                    writer.println(preset);
                }
            }
        }
        else{
            writer.println("Keine aktiven Chats... Starte einen chat mit /chat <Benutzername>!");
        }

        writer.flush();
    }

    public void addActiveChat(Chat chat){
        chatliste.add(chat);
    }

    public void deleteActiveChat(User chatpartner){
        for(Chat chat : chatliste){
            if( ( (chat.getMEMBER()[0] == chatpartner) && (chat.getMEMBER()[1] == this) ) || ( (chat.getMEMBER()[1] == chatpartner) && (chat.getMEMBER()[0] == this) ) ){
                chatliste.remove(chat);
            }
        }
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
