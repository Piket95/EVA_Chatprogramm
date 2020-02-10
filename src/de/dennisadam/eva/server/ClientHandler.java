package de.dennisadam.eva.server;

import de.dennisadam.eva.chat.Chat;
import de.dennisadam.eva.chat.ChatStatus;
import de.dennisadam.eva.chat.Messages;
import de.dennisadam.eva.user.User;
import de.dennisadam.eva.user.UserStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private Socket client;
    private String username;
    private User currentUser;

    private PrintWriter writer;
    private BufferedReader reader;
    private String line;

    public ClientHandler(Socket client){
        try{
            this.client = client;
            this.writer = new PrintWriter(client.getOutputStream());
            this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            //Bitte den Client um Login
            while (!login());

            //Warte auf Befehle
            while((line = reader.readLine()) != null){

                if(line.contains("/chat") && !line.equals("/chatlist")){
                    User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));

                    chatting(chatpartner);
                }
                else{
                    switch (line){
                        case "/help":
                            printCommandList();
                            break;
                        case "/userlist":
                            writer.println("Liste aller Benutzer:");

                            for(User u : Server.userList.getUserList()){
                                String preset = u.getUsername();

                                if(u.getStatus() == UserStatus.ONLINE){
                                    preset = preset + "(online)";
                                }

                                if(u.getUsername().equals(currentUser.getUsername())){
                                    preset = preset + " --> YOU";
                                }

                                writer.println(preset);
                            }

                            writer.flush();
                            break;
                        case "/chatlist":
                            currentUser.showChatList();
                            break;
                        case "/deleteChat":
                            User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));

                            //Der Chat wird für beide Gesprächspartner gelöscht!
                            currentUser.deleteActiveChat(chatpartner);
                            chatpartner.deleteActiveChat(currentUser);
                            break;
                        case "/logout":
                            logout();
                            login();
                            break;
                        case "/exit":
                            disconnectClient();
                            break;
                        default:
                            writer.println("Der Befehl existiert nicht! Vielleicht hast du dich vertippt?");
                            writer.flush();
                    }
                }

                if(currentUser.getStatus() == UserStatus.OFFLINE){
                    break;
                }
            }

        } catch (IOException e) {
            if(e instanceof SocketException){
                currentUser.setStatus(UserStatus.OFFLINE);
                System.err.println("Die Verbindung zum Client wurde unerwartet beendet!");
            }
            else{
                e.printStackTrace();
            }
        } finally{
            try{
                writer.close();
                reader.close();
                client.close();
            } catch (IOException e) {
                System.err.println("Schließen der Streams nicht möglich!");
                e.printStackTrace();
            }
        }
    }

    public void chatting(User chatpartner){

        if(chatpartner == null){
            writer.println("Den angegebenen Benutzer gibt es nicht! Du findest eine Liste mit allen registrierten Benutzern unter /userlist!");
            writer.flush();
        }
        else if(chatpartner == currentUser){
            writer.println("Du kannst keinen Chat mit dir selbst starten!");
            writer.flush();
        }
        else{
            //Starte Chat
            Chat chat = currentUser.chatExists(new User[]{currentUser, chatpartner});

            if(chat == null){
                chat = new Chat(currentUser, chatpartner);
                currentUser.addActiveChat(chat);
                chatpartner.addActiveChat(chat);
            }

            writer.println("Chat mit " + chatpartner.getUsername() + " gestartet!");
            if(chat.getMEMBER()[0] == currentUser){
                chat.setUserChatStatus0(ChatStatus.JOINED);
            }
            else {
                chat.setUserChatStatus1(ChatStatus.JOINED);
            }

            if(chat.countNewMessages(currentUser) != 0){
                writer.println("Folgende neuen Nachrichten in Abwesenheit: ");

                for(Messages messages : chat.getNewMessages()){
                    writer.println("[" + messages.getTimestamp() + "] " + messages.getSender().getUsername() + ": " + messages.getMessage());
                }

                writer.flush();
            }

            //TODO: Joined Nachricht, wenn jemand den Chat betritt

            if(chat.countNewMessages(currentUser) == 0){
                writer.println("Es liegen keine neuen Nachrichten vor!");
            }
            writer.println("Mithilfe von /help bekommen Sie eine Liste mit Befehlen, die während des Chats zur Verfügung stehen!");
            writer.flush();

            try{
                while((line = reader.readLine()) != null){
                    if(line.equals("/leave")){

                        if(chat.getMEMBER()[0] == currentUser){
                            chat.setUserChatStatus0(ChatStatus.LEFT);
                        }
                        else {
                            chat.setUserChatStatus1(ChatStatus.LEFT);
                        }

                        break;
                    }
                    else if(line.contains("/")){
                        switch (line){
                            case "/help":
                                writer.println(chat.getCommandList());
                                writer.flush();
                                break;
                            case "/archiv":
                                writer.println(chat.getArchiv());
                                writer.flush();
                                break;
                            default:
                                writer.println("Der Befehl existiert nicht! Vielleicht hast du dich vertippt?");
                                writer.flush();
                        }
                    }
                    else{
                        chat.sendMessage(new Messages(currentUser, line));
                    }
                }
            } catch (IOException e) {
                if(e instanceof SocketException){
                    System.out.println("Die Verbindung zum Client wurde unerwartet beendet!");
                }
                else{
                    e.printStackTrace();
                }
            }

            writer.println("Du hast den Chat verlassen!");
            writer.flush();

        }
    }

    public void printCommandList(){
        writer.println("------------------------Liste der Befehle------------------------");
        writer.println("/help\t\t\t\t\t\t\tZeigt die Liste der verfügbaren Befehle (diese hier)");
        writer.println("/userlist\t\t\t\t\t\tZeigt eine Liste der verfügbaren Benutzer an");
        writer.println("/chatlist\t\t\t\t\t\tZeigt die Liste deiner aktiven Chats");
        writer.println("/deleteChat <Benutzername>\t\tLöscht den aktiven Chat mit dem angegebenen Benutzer aus der Liste (case sensitive");
        writer.println("/chat <Benutzername>\t\t\tStarte einen Chat mit dem angegebenen Benutzer (case sensitive)");
        writer.println("/logout\t\t\t\t\t\t\tMeldet dich vom Server ab");
        writer.println("/exit\t\t\t\t\t\t\tMeldet dich vom Server ab falls noch nicht geschehen und beendet die Anwendung");
        writer.println("-----------------------------------------------------------------");
        writer.flush();
    }

    //TODO: Abfrage ob Login oder Registrieren
    public boolean login(){
        try{
            writer.println("Bitte gib deinen Benutzernamen ein: (Achtung: case sensitive)");
            writer.flush();

            //Abfrage, ob username schon in Liste existiert oder dieser komplett neu angelegt werden muss
            String line;
            if((line = reader.readLine()) != null){
                username = line;
                currentUser = Server.userList.getUserFromList(username);

                if(currentUser == null){
                    currentUser = new User(username, writer, reader);
                    Server.userList.add(currentUser);
                }
                else {
                    currentUser.setStatus(UserStatus.ONLINE);
                    currentUser.setReader(reader);
                    currentUser.setWriter(writer);
                }
            }

            if(currentUser.getStatus() == UserStatus.ONLINE){
                System.out.println("[Server/TCP] \"" + username + "\" (" + client.getRemoteSocketAddress() + ") erfolgreich angemeldet!");
                writer.println("\"" + username + "\" erfolgreich angemeldet!\n");
                writer.flush();

                //Gibt eine Liste der aktiven (bereits in der Vergangenheit getätigten) Chats mit anderen Personen
                currentUser.showChatList();

                writer.println("Mithilfe von /help, bekommen Sie eine Übersicht der verfügbaren Befehle!");
                writer.flush();

                return true;
            }else{
                System.out.println("[Server/TCP] Anmeldung von \"" + username + "\" (" + client.getRemoteSocketAddress() + ") fehlgeschlagen!");
                writer.println("Anmeldung fehlgeschlagen!");
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void logout(){

        System.out.println("[Server/TCP] \"" + currentUser.getUsername() + "\" meldet sich nun ab!");
        writer.println("Sie werden nun vom Server abgemeldet!");
        writer.flush();

        currentUser.setStatus(UserStatus.OFFLINE);
    }

    public void disconnectClient(){

        try {

            if(currentUser.getStatus() == UserStatus.ONLINE){
                logout();
            }

            writer.println("Die Verbindung zum Server wird nun getrennt!");
            writer.println("SHUTDOWN");
            writer.flush();

            currentUser.setWriter(null);
            currentUser.setReader(null);

            writer.close();
            reader.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server/TCP] Verbindung zum Client abgebaut!");
    }
}
