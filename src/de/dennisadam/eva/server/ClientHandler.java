package de.dennisadam.eva.server;

import de.dennisadam.eva.server.chat.Chat;
import de.dennisadam.eva.server.chat.ChatStatus;
import de.dennisadam.eva.server.chat.Messages;
import de.dennisadam.eva.server.user.User;
import de.dennisadam.eva.server.user.UserStatus;

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

        //TODO: Vor und nach jedem Output an Client eine leere Zeile (\n) mitsenden (Übersicht)
        try{
            //Bitte den Client um Login
            while (!login());

            //Warte auf Befehle
            while((line = reader.readLine()) != null){

                if(line.contains("/chat") && !line.equals("/chatlist")){
                    User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));

                    startChat(chatpartner);
                }
                else{
                    switch (line){
                        case "/help":
                            printCommandlist();
                            break;
                        case "/userlist":
                            printUserList();
                            break;
                        case "/chatlist":
                            currentUser.showChatList();
                            break;
                        case "/deleteChat":
                            User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));

                            //Der Chat wird für beide Gesprächspartner gelöscht!
                            currentUser.deleteActiveChat(chatpartner);
                            chatpartner.deleteActiveChat(currentUser);

                            writer.println();
                            writer.println("Chat mit \"" + chatpartner.getUsername() + "\" wurde erfolgreich vom Server gelöscht!");
                            writer.flush();
                            break;
                        case "/logout":
                            logout();
                            login();
                            break;
                        case "/exit":
                            disconnectClient();
                            break;
                        default:
                            writer.println();
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
                if(currentUser != null){
                    currentUser.setStatus(UserStatus.OFFLINE);
                    System.err.println("Die Verbindung zum Client \"" + currentUser.getUsername() + "\" wurde unerwartet beendet!");
                }
                else{
                    System.err.println("Die Verbindung zum Client (" + client.getInetAddress().toString().substring(client.getInetAddress().toString().lastIndexOf("/") + 1) + ") wurde unerwartet beendet!");
                }
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

    public void startChat(User chatpartner) throws IOException {

        if(chatpartner == null){
            writer.println();
            writer.println("Den angegebenen Benutzer gibt es nicht! Du findest eine Liste mit allen registrierten Benutzern unter /userlist!");
            writer.flush();
        }
        else if(chatpartner == currentUser){
            writer.println();
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

            writer.println();
            writer.println("Chat mit \"" + chatpartner.getUsername() + "\" gestartet!");
            chat.joinChat(currentUser);

            writer.println();
            if(chat.countNewMessages(currentUser) != 0){
                writer.println("-----------------Neue Nachrichten in Abwesenheit-----------------");

                for(Messages messages : chat.getNewMessages()){
                    writer.println("[" + messages.getTimestamp() + "] " + messages.getSender().getUsername() + ": " + messages.getMessage());
                }

                writer.println("-----------------------------------------------------------------");
            }
            writer.println("Mithilfe von /help bekommst du eine Liste mit Befehlen, die während des Chats zur Verfügung stehen!");
            writer.flush();

            try{
                while((line = reader.readLine()) != null){

                    if(line.contains("/leave")){
                        chat.leaveChat(currentUser);
                        break;
                    }
                    else if(line.contains("/")){
                        switch (line){
                            case "/help":
                                chat.getCommandList(writer);
                                break;
                            case "/archiv":
                                chat.getArchiv(writer);
                                break;
                            default:
                                writer.println();
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
                    chat.userDisconnectError(currentUser);
                    throw e;
                }
                else{
                    e.printStackTrace();
                }
            }
            finally{
                writer.println("Du hast den Chat verlassen!");
                writer.flush();
            }

        }
    }

    public void printCommandlist(){
        writer.println();
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

    public void printUserList(){
        writer.println();
        writer.println("----------------------Liste aller Benutzer-----------------------");

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

        writer.println("-----------------------------------------------------------------");
        writer.flush();
    }

    //TODO: Abfrage ob Login oder Registrieren (exit?!)
    public boolean login() throws IOException {
        writer.println();
        writer.println("Bitte gib deinen Benutzernamen ein: (Achtung: case sensitive)");
        writer.flush();

        //Abfrage, ob username schon in Liste existiert oder dieser komplett neu angelegt werden muss
        String line;
        if((line = reader.readLine()) != null){

            if(!line.matches("^([a-zA-Z]*|\\d*)+$")){
                writer.println();
                writer.println("Der Benutzername darf nur Buchstaben und Zahlen enthalten. Keine Sonderzeichen!");
                writer.flush();

                return false;
            }

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

        //TODO: Passwortabfrage irgendwie einbauen
        if(currentUser.getStatus() == UserStatus.ONLINE){
            System.out.println("[Server/TCP] \"" + username + "\" (" + client.getInetAddress().toString().substring(client.getInetAddress().toString().lastIndexOf("/") + 1) + ") erfolgreich angemeldet!");
            writer.println();
            writer.println("\"" + username + "\" erfolgreich angemeldet!\n");
            writer.flush();

            //Gibt eine Liste der aktiven (bereits in der Vergangenheit getätigten) Chats mit anderen Personen
            currentUser.showChatList();

            writer.println();
            writer.println("Mithilfe von /help, bekommst du eine Übersicht aller verfügbaren Befehle!");
            writer.flush();

            return true;
        }else{
            System.out.println("[Server/TCP] Anmeldung von \"" + username + "\" (" + client.getInetAddress().toString().substring(client.getInetAddress().toString().lastIndexOf("/") + 1) + ") fehlgeschlagen!");
            writer.println("Anmeldung fehlgeschlagen! Bitte versuche es erneut!");
            writer.flush();
        }

        return false;
    }

    public void logout(){

        System.out.println("[Server/TCP] \"" + currentUser.getUsername() + "\" meldet sich nun ab!");

        writer.println();
        writer.println("Sie werden nun vom Server abgemeldet!");
        writer.flush();

        currentUser.setWriter(null);
        currentUser.setReader(null);

        currentUser.setStatus(UserStatus.OFFLINE);
    }

    public void disconnectClient(){

        try {

            if(currentUser != null && (currentUser.getStatus() == UserStatus.ONLINE)){
                logout();
            }

            writer.println();
            writer.println("Die Verbindung zum Server wird nun getrennt!");
            writer.println("EXIT");
            writer.flush();

            writer.close();
            reader.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server/TCP] Verbindung zum Client abgebaut!");
    }
}
