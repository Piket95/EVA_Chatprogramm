package de.dennisadam.eva.server.user;

import de.dennisadam.eva.server.chat.Chat;
import de.dennisadam.eva.server.chat.ChatMember;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private byte[] password;
    private UserStatus status;

    private PrintWriter writer;
    private BufferedReader reader;

    private List<Chat> chatliste;

    public User(String username, byte[] password, PrintWriter writer, BufferedReader reader) {
        this.username = username;
        this.password = password;
        this.status = UserStatus.ONLINE;
        this.reader = reader;
        this.writer = writer;

        this.chatliste = new ArrayList<>();
    }

    public Chat chatExists(User user1, User user2){
        for(Chat chat : this.chatliste){
            User member1 = chat.getCHATMEMBER().get(0).getUser();
            User member2 = chat.getCHATMEMBER().get(1).getUser();

            if( ((user1 == member1) && (user2 == member2)) || ((user1 == member2) && (user2 == member1)) ){
                return chat;
            }
        }

        return null;
    }

    public void printChatList(){
        writer.println();
        writer.println("---------------------Liste der aktiven Chats---------------------");

        if(this.getChatliste().size() != 0){
            for(Chat chat : this.getChatliste()){
                String preset = "- ";

                ChatMember thisMember;
                ChatMember partner;
                if(chat.getCHATMEMBER().get(0).getUser() == this){
                    thisMember = chat.getCHATMEMBER().get(0);
                    partner = chat.getCHATMEMBER().get(1);
                }
                else{
                    thisMember = chat.getCHATMEMBER().get(1);
                    partner = chat.getCHATMEMBER().get(0);
                }

                if(partner.getUser().getStatus() == UserStatus.ONLINE){
                    preset = preset + "(*)";
                }

                preset = preset + partner.getUser().getUsername();

                int newMessages = thisMember.getNewMessagesSize();
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

        writer.println("-----------------------------------------------------------------");
        writer.flush();
    }

    public void deleteActiveChat(User chatpartner){
        for(Chat chat : chatliste){

            for(ChatMember chatMember : chat.getCHATMEMBER()){
                if(chatMember.getUser() == chatpartner){
                    chatliste.remove(chat);
                    chatpartner.chatliste.remove(chat);
                }
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

    public List<Chat> getChatliste() {
        return chatliste;
    }

    public byte[] getPassword() {
        return password;
    }
}
