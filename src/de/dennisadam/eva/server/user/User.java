package de.dennisadam.eva.server.user;

import de.dennisadam.eva.server.chat.Chat;
import de.dennisadam.eva.server.chat.ChatMember;
import de.dennisadam.eva.server.chat.ChatStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class User {
    private final String username;
    private final byte[] password;
    private UserStatus status;

    private PrintWriter writer;

    private final List<Chat> chatliste;

    public User(String username, byte[] password, PrintWriter writer) {
        this.username = username;
        this.password = password;
        this.status = UserStatus.ONLINE;
        this.writer = writer;

        this.chatliste = new ArrayList<>();
    }

    public Chat chatExists(User user1, User user2){
        for(Chat chat : this.chatliste){
            ChatMember member1 = chat.getChatMemberByUser(user1);
            ChatMember member2 = chat.getChatMemberByUser(user2);

            if(member1 != null && member2 != null){
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

                ChatMember thisMember = chat.getChatMemberByUser(this);
                ChatMember partner = chat.getChatPartnerOf(this);

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

    public void deleteActiveChat(User currentUser, User chatpartner) {
        if(chatliste.size() != 0){
            Iterator<Chat> iter = chatliste.iterator();
            Chat chat;

            while (iter.hasNext()) {
                chat = iter.next();

                ChatMember partner = chat.getChatMemberByUser(chatpartner);
                ChatMember currentMember = chat.getChatMemberByUser(currentUser);

                if((partner != null && currentMember != null)){
                    if (partner.getStatus() == ChatStatus.JOINED) {
                        //Befindet sich der Partner noch im Chat, wird er rausgeschmissen und der Chat auf beiden Seiten gelöscht
                        partner.getUser().getWriter().println();
                        partner.getUser().getWriter().println("Du wurdest aus dem aktuellen Chat geworfen, da der Gesprächspartner diesen gelöscht hat!");
                        chat.leaveChat(partner, currentMember);
                        partner.getUser().getWriter().println("EXITCHAT");
                        partner.getUser().getWriter().flush();
                    }
                    iter.remove();
                    chatpartner.chatliste.remove(chat);

                    writer.println();
                    writer.println("Chat mit \"" + chatpartner.getUsername() + "\" wurde erfolgreich vom Server gelöscht!");
                    writer.flush();
                }
                else{
                    writer.println();
                    writer.println("Ein Chat mit \"" + chatpartner.getUsername() + "\" existiert nicht in deiner Chatliste!");
                    writer.flush();
                }
            }
        }
        else{
            writer.println();
            writer.println("Es kann kein Chat gelöscht werden, da deine Chatliste leer ist.");
            writer.println("Starte einen neuen chat mit /chat <Benutzername>");
            writer.flush();
        }
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public String getUsername() {
        return username;
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
