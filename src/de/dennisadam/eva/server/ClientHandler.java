package de.dennisadam.eva.server;

import de.dennisadam.eva.server.chat.Chat;
import de.dennisadam.eva.server.chat.Messages;
import de.dennisadam.eva.server.user.User;
import de.dennisadam.eva.server.user.UserStatus;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private boolean isTerminated;

    private Socket client;
    private User currentUser;

    private PrintWriter writer;
    private BufferedReader reader;
    private String line;

    public ClientHandler(Socket client){
        try{
            isTerminated = false;

            this.client = client;
            this.writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
            this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            while(!isTerminated){

                //Einloggen, Registrieren, Exit -----------------------------------------------------------------------------------------------
                while(currentUser == null){
                    User checkUser = null;

                    writer.println();
                    writer.println("Was möchstest du tun? (Zahl eingeben)");
                    writer.println("(1) Einloggen");
                    writer.println("(2) Registrieren");
                    writer.println("(3) Programm beenden");
                    writer.flush();

                    //Prüfen, welche Option gewält wurde

                    if((line = reader.readLine()) != null){
                        try{
                            int option = Integer.parseInt(line);

                            if(option >= 1 && option <= 3){
                                if (option == 1) { //Einloggen

                                    while (currentUser == null) {
                                        writer.println();
                                        writer.println("Bitte gib deinen Benutzernamen ein:");
                                        writer.flush();

                                        if ((line = reader.readLine()) != null) {

                                            if (line.equals("/back")) {
                                                break;
                                            }

                                            String username = line;

                                            writer.println();
                                            writer.println("Bitte gib dein Passwort ein:");
                                            writer.flush();

                                            if ((line = reader.readLine()) != null) {

                                                if (line.equals("/back")) {
                                                    break;
                                                }

                                                String password = line;
                                                checkUser = login(username, password);

                                                if (checkUser != null) {
                                                    currentUser = checkUser;
                                                    currentUser.setReader(reader);
                                                    currentUser.setWriter(writer);

                                                    writer.println();
                                                    writer.println("Du hast dich erfolgreich unter dem Benutzernamen " + currentUser.getUsername() + " angemeldet!");
                                                    writer.flush();

                                                    //Anzeige der Liste mit den aktiven Chats dieses Users
                                                    currentUser.showChatList();

                                                    writer.println();
                                                    writer.println("Mithilfe von /help, bekommst du eine Übersicht aller verfügbaren Befehle!");
                                                } else {
                                                    writer.println();
                                                    writer.println("Anmeldung fehlgeschlagen! Bitte versuche es erneut!");
                                                    writer.println("Mit /back kommst du zurück zum Anmeldemenü.");
                                                }
                                                writer.flush();
                                            }
                                        }
                                    }
                                }
                                else if (option == 2) { //Registrieren
                                    while(currentUser == null){
                                        writer.println();
                                        writer.println("Bitte gib deinen gewünschten Benutzernamen ein:");
                                        writer.flush();

                                        if ((line = reader.readLine()) != null) {

                                            if (line.equals("/back")) {
                                                break;
                                            }
                                            else if(line.matches("^([a-zA-Z]*|\\d*)+$")){
                                                String username = line;

                                                writer.println();
                                                writer.println("Bitte gib dein gewünschtes Passwort ein:");
                                                writer.flush();

                                                if ((line = reader.readLine()) != null) {
                                                    if (line.equals("/back")) {
                                                        break;
                                                    }

                                                    String password = line;

                                                    writer.println();
                                                    writer.println("Bitte wiederhole dein Passwort:");
                                                    writer.flush();

                                                    if((line = reader.readLine()) != null){
                                                        if(line.equals("/back")){
                                                            break;
                                                        }
                                                        else if (line.equals(password)){
                                                            checkUser = register(username, password);

                                                            if (checkUser != null) {
                                                                currentUser = checkUser;
                                                                Server.userList.add(currentUser);

                                                                writer.println();
                                                                writer.println("Du hast dich erfolgreich unter dem Benutzernamen " + currentUser.getUsername() + " angemeldet!");
                                                                writer.flush();

                                                                //Anzeige der Liste mit den aktiven Chats dieses Users
                                                                currentUser.showChatList();

                                                                writer.println();
                                                                writer.println("Mithilfe von /help, bekommst du eine Übersicht aller verfügbaren Befehle!");
                                                            } else {
                                                                writer.println();
                                                                writer.println("Anmeldung fehlgeschlagen! Bitte versuche es erneut!");
                                                                writer.println("Mit /back kommst du zurück zum Anmeldemenü.");
                                                            }
                                                            writer.flush();
                                                        }
                                                        else{
                                                            writer.println();
                                                            writer.println("Die beiden eingegebenen Passwörter stimmen nicht überein! Bitte versuche es erneut");
                                                            writer.flush();
                                                        }
                                                    }
                                                }
                                            }
                                            else{
                                                writer.println();
                                                writer.println("Der Benutzername darf nur Buchstaben und Zahlen enthalten. Bitte keine Sonderzeichen verwenden!");
                                                writer.flush();
                                            }
                                        }
                                    }
                                }
                                else if (option == 3) { //Beenden
                                    disconnectClient();
                                    break;
                                }
                            }
                            else{
                                writer.println();
                                writer.println("Bitte gib eine Zahl zwischen 1 und 3 ein!");
                                writer.flush();
                            }
                        } catch (NumberFormatException e) {
                            writer.println();
                            writer.println("Bitte gib eine Zahl zwischen 1 und 3 ein!");
                            writer.flush();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Befehle erwarten ---------------------------------------------------------------------------------------------------------------
                while(!isTerminated){
                    if((line = reader.readLine()) != null){
                        if(line.startsWith("/chat ") && !line.equals("/chatlist")){
                            User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));
                            startChat(chatpartner);
                        }
                        else if(line.startsWith("/deleteChat ")){
                            User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));

                            //Der Chat wird für beide Gesprächspartner gelöscht!
                            currentUser.deleteActiveChat(chatpartner);
                            chatpartner.deleteActiveChat(currentUser);

                            writer.println();
                            writer.println("Chat mit \"" + chatpartner.getUsername() + "\" wurde erfolgreich vom Server gelöscht!");
                            writer.flush();
                            break;
                        }
                        else if(line.equals("/logout")){
                            logout();
                            break;
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
                                case "/exit":
                                    disconnectClient();
                                    break;
                                default:
                                    writer.println();
                                    writer.println("Der Befehl existiert nicht! Vielleicht hast du dich vertippt?");
                                    writer.flush();
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            //Fehlerbehandlung, wenn Verbindung zum Client abbricht
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
                        //TODO: auch möglich, wenn Gesprächspartner noch im Chat ist
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

    public User login(String username, String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        User loginUser;
        byte[] hashedPassword;

        //Prüfen ob User existiert
        loginUser = Server.userList.getUserFromList(username);

        if(loginUser == null){
            writer.println();
            writer.println("Der angegebene User existiert nicht in der Datenbank!");
            writer.flush();

            return null;
        }

        hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

        if(Arrays.equals(hashedPassword, loginUser.getPassword())){
            return loginUser;
        }

        return null;
    }

    public User register(String username, String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        User regUser = Server.userList.getUserFromList(username);

        if(regUser == null){
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            return new User(username, hashedPassword, writer, reader);
        }
        else{
            writer.println();
            writer.println("Der eingegebene Benutzername existiert bereits in der Datenbank! Bitte wähle einen anderen.");
            writer.flush();
        }

        return null;
    }

    public void logout(){

        System.out.println("[Server/TCP] \"" + currentUser.getUsername() + "\" meldet sich nun ab!");

        writer.println();
        writer.println("Sie werden nun vom Server abgemeldet!");
        writer.flush();

        currentUser.setWriter(null);
        currentUser.setReader(null);
        currentUser.setStatus(UserStatus.OFFLINE);
        currentUser = null;
    }

    public void disconnectClient(){

        if(currentUser != null){
            logout();
        }

        writer.println("EXIT");
        writer.flush();

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(currentUser != null){
            System.err.println("[Server/TCP] Benutzer \"" + currentUser.getUsername() + "\" hat die Verbindung zum Server erfolgreich getrennt!");
            System.out.println("Thread von User \"" + currentUser.getUsername() + "\" wurde erfolgreich beendet!");
        }
        else{
            System.err.println("[Server/TCP] Benutzer \"" + client.getInetAddress().toString().substring(client.getInetAddress().toString().lastIndexOf("/") + 1) +":" + client.getPort() + "\" hat die Verbindung zum Server erfolgreich getrennt!");
            System.out.println("Thread von Client \"" + client.getInetAddress().toString().substring(client.getInetAddress().toString().lastIndexOf("/") + 1) +":" + client.getPort() + "\" wurde erfolgreich beendet!");
        }

        isTerminated = true;

    }
}
