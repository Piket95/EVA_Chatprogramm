package de.dennisadam.eva.server.chat;

import de.dennisadam.eva.server.user.User;
import de.dennisadam.eva.server.user.UserStatus;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Chat {

    @SuppressWarnings("SpellCheckingInspection")
    private final List<ChatMember> CHATMEMBER;
    @SuppressWarnings("FieldCanBeLocal")
    private final int ARCHIVE_SIZE = 10;

    private final List<Message> archive;

    public Chat(User user1, User user2) {
        this.CHATMEMBER = new ArrayList<>();
        CHATMEMBER.add(new ChatMember(user1));
        CHATMEMBER.add(new ChatMember(user2));
        this.archive = new ArrayList<>();
    }

    public synchronized void sendMessage(ChatMember partner, Message message){
        if((partner.getStatus() == ChatStatus.LEFT) && (partner.getUser().getStatus() == UserStatus.ONLINE)){
            partner.addNewMessage(message);
            message.getSender().getWriter().println(message.getMessage());
            message.getSender().getWriter().flush();

            partner.getUser().getWriter().println();
            partner.getUser().getWriter().println("Du hast eine neue Nachricht von \"" + message.getSender().getUsername() + "\" erhalten!");
            partner.getUser().getWriter().println("Gib /chat " + message.getSender().getUsername() + " ein, um dem Chat beizutreten.");
            partner.getUser().getWriter().flush();
        }
        else if(partner.getUser().getStatus() == UserStatus.OFFLINE){
            partner.addNewMessage(message);
            message.getSender().getWriter().println();
            message.getSender().getWriter().println("Dein Gesprächspartner ist momentan Offline.");
            message.getSender().getWriter().println("Die Nachricht wird zugestellt, sobald er wieder online kommt!");
            message.getSender().getWriter().flush();
        }
        else{
            archive.add(message);
            checkArchiveSize();

            partner.getUser().getWriter().println(message.getMessage());
            partner.getUser().getWriter().flush();

            message.getSender().getWriter().println(message.getMessage());
            message.getSender().getWriter().flush();
        }
    }

    //Wenn mehr als 10 Nachrichten im Archiv hinterlegt sind, wird die 1. gelöscht, damit insgesamt nur 10 Nachrichten in der Liste sind
    public synchronized void checkArchiveSize(){
        while (archive.size() > ARCHIVE_SIZE) {
            archive.remove(0);
        }
    }

    public synchronized void printArchive(PrintWriter writer){

        writer.println();
        writer.println("------------Letzten 10 Nachrichten dieser Konversation-----------");
        if(archive.size() == 0){
            writer.println("Archiv ist leer! Möglicherweise hat vorher noch kein Nachrichtenaustausch stattgefunden!");
        }
        else{
            for(Message message : archive){
                writer.println(message.getMessage());
            }
        }
        writer.println("------------------------------------------------------------------");
        writer.flush();
    }

    public synchronized void addArchiveMessage(Message message){
        archive.add(message);
    }

    public ChatMember getChatMemberByUser(User user){
        for(ChatMember member : CHATMEMBER){
            if(member.getUser() == user){
                return member;
            }
        }

        return null;
    }

    public ChatMember getChatPartnerOf(User currentUser){
        if(CHATMEMBER.get(0).getUser() == currentUser){
            return CHATMEMBER.get(1);
        }
        else if(CHATMEMBER.get(1).getUser() == currentUser){
            return CHATMEMBER.get(0);
        }
        else{
            return null;
        }
    }

    public void printCommandList(PrintWriter writer){

        writer.println();
        writer.println("----------------------Liste der Chat-Befehle---------------------");
        writer.println("/help\t\t\t\t\t\tZeigt die Liste der verfügbaren Befehle (diese hier)");
        writer.println("/archiv\t\t\t\t\t\tZeigt die letzten 10 Nachrichten an");
        writer.println("/leave\t\t\t\t\t\tVerlasse den aktuellen Chat");
        writer.println("-----------------------------------------------------------------");
        writer.flush();

    }

    public void joinChat(ChatMember joinedUser, ChatMember partner){
        joinedUser.setStatus(ChatStatus.JOINED);

        joinedUser.getUser().getWriter().println();
        joinedUser.getUser().getWriter().println("Du bist dem Chat mit \"" + partner.getUser().getUsername() + "\" erfolgreich beigetreten!");
        joinedUser.getUser().getWriter().flush();

        if(partner.getStatus() == ChatStatus.JOINED){
            partner.getUser().getWriter().println();
            partner.getUser().getWriter().println("\"" + joinedUser.getUser().getUsername() + "\" ist dem Chat beigetreten!");
            partner.getUser().getWriter().flush();
        }
    }

    public void leaveChat(ChatMember leavingUser, ChatMember partner){
        leaveChat(leavingUser, partner,false);
    }

    public void leaveChat(ChatMember leavingUser, ChatMember partner, boolean disconnect){
        leavingUser.setStatus(ChatStatus.LEFT);

        leavingUser.getUser().getWriter().println();
        leavingUser.getUser().getWriter().println("Du hast den Chat mit \"" + partner.getUser().getUsername() + "\" verlassen!");
        leavingUser.getUser().getWriter().flush();

        if(partner.getStatus() == ChatStatus.JOINED){
            if(disconnect){
                partner.getUser().getWriter().println();
                partner.getUser().getWriter().println("\"" + leavingUser.getUser().getUsername() + "\" hat die Verbindung zum Server verloren!");
            }
            else{
                partner.getUser().getWriter().println();
                partner.getUser().getWriter().println("\"" + leavingUser.getUser().getUsername() + "\" hat den Chat verlassen!");
            }
            partner.getUser().getWriter().flush();
        }
    }
}
