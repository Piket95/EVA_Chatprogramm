package de.dennisadam.eva.server;

import de.dennisadam.eva.server.chat.Chat;
import de.dennisadam.eva.server.chat.ChatMember;
import de.dennisadam.eva.server.chat.ChatStatus;
import de.dennisadam.eva.server.chat.Message;
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
            this.reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
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
                    User checkUser;

                    writer.println();
                    writer.println("Was möchtest du tun? (Zahl eingeben)");
                    writer.println("(1) Einloggen");
                    writer.println("(2) Registrieren");
                    writer.println("(3) Programm beenden");
                    writer.flush();

                    //Prüfen, welche Option gewählt wurde
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
                                                    currentUser.setWriter(writer);
                                                    currentUser.setStatus(UserStatus.ONLINE);

                                                    writer.println();
                                                    writer.println("Du hast dich erfolgreich unter dem Benutzernamen " + currentUser.getUsername() + " angemeldet!");
                                                    writer.flush();
                                                    System.out.println("[Server/TCP] \"" + currentUser.getUsername() + "\" hat sich am System angemeldet!");

                                                    //Anzeige der Liste mit den aktiven Chats dieses Users
                                                    currentUser.printChatList();

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
                                        else{
                                            throw new SocketException("Client shutdown");
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
                                                                System.out.println("[Server/TCP] \"" + currentUser.getUsername() + "\" hat sich am System registriert!");

                                                                //Anzeige der Liste mit den aktiven Chats dieses Users
                                                                currentUser.printChatList();

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
                                        else{
                                            throw new SocketException("Client shutdown");
                                        }
                                    }
                                }
                                else { //Beenden
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
                    else{
                        throw new SocketException("Client shutdown");
                    }
                }

                //Befehle erwarten ---------------------------------------------------------------------------------------------------------------
                while(!isTerminated){
                    if((line = reader.readLine()) != null){
                        if(line.equals("EXITCHAT")){
                            break;
                        }
                        else if(line.startsWith("/chat ") && !line.equals("/chatlist")){
                            User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));
                            startChat(chatpartner);
                        }
                        else if(line.startsWith("/deleteChat ")){
                            User chatpartner = Server.userList.getUserFromList(line.substring(line.lastIndexOf(" ") + 1));

                            if(chatpartner != null){
                                //Der Chat wird für beide Gesprächspartner gelöscht!
                                currentUser.deleteActiveChat(currentUser, chatpartner);
                            }
                            else{
                                writer.println();
                                writer.println("Der angegebene Nutzer existiert nicht!");
                                writer.flush();
                            }
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
                                    currentUser.printChatList();
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
                    else{
                        throw new SocketException("Client shutdown");
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
            //Checke ob Chat bereits existiert oder nicht
            Chat chat = currentUser.chatExists(currentUser, chatpartner);

            if (chat == null) {
                chat = new Chat(currentUser, chatpartner);
                currentUser.getChatliste().add(chat);
                chatpartner.getChatliste().add(chat);
            }

            ChatMember currentMember = chat.getChatMemberByUser(currentUser);
            ChatMember partner = chat.getChatMemberByUser(chatpartner);

            //Starte Chat
            chat.joinChat(currentMember, partner);
            currentMember.printNewMessages(chat);

            writer.println();
            writer.println("Mithilfe von /help, bekommst du eine Übersicht aller verfügbaren Befehle!");
            writer.flush();

            try{
                //Endlos warten auf Input
                while(true){
                    if((line = reader.readLine()) != null){
                        //Prüfen ob aktueller Nutzer aus Chat rausgeschmissen wurde
                        if(currentMember.getStatus() == ChatStatus.LEFT){
                            break;
                        }

                        //Commands
                        if(line.startsWith("/")){
                            if(line.equals("/leave")){
                                chat.leaveChat(currentMember, partner);
                                break;
                            }
                            else{
                                switch (line) {
                                    case "/help":
                                        chat.printCommandList(writer);
                                        break;
                                    case "/archiv":
                                        chat.printArchive(writer);
                                        break;
                                    default:
                                        writer.println();
                                        writer.println("Der Befehl existiert nicht! Vielleicht hast du dich vertippt?");
                                        writer.flush();
                                }
                            }
                        }
                        else{
                            if(!line.equals("")){
                                chat.sendMessage(partner, new Message(currentUser, line));
                            }
                        }
                    }
                    else{
                        throw new SocketException("Client shutdown");
                    }
                }
            } catch (IOException e){
                chat.leaveChat(currentMember, partner, true);
                throw e;
            }

            //TODO: Client shutdown???
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

        if(username.equals("")){
            writer.println();
            writer.println("Bitte gibt einen Benutzernamen an!");
            writer.flush();
            return null;
        }

        if(password.equals("")){
            writer.println();
            writer.println("Bitte gib ein Passwort an!");
            writer.flush();
            return null;
        }

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
        else if(loginUser.getStatus() == UserStatus.ONLINE){
            writer.println();
            writer.println("Der angegebene User ist bereits angemeldet!");
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
        if(username.equals("")){
            writer.println();
            writer.println("Bitte gibt einen Benutzernamen an!");
            writer.flush();
            return null;
        }

        if(password.equals("")){
            writer.println();
            writer.println("Bitte gib ein Passwort an!");
            writer.flush();
            return null;
        }

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        User regUser = Server.userList.getUserFromList(username);

        if(regUser == null){
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            return new User(username, hashedPassword, writer);
        }
        else{
            writer.println();
            writer.println("Der eingegebene Benutzername existiert bereits in der Datenbank! Bitte wähle einen anderen.");
            writer.flush();
        }

        return null;
    }

    public void logout(){

        System.out.println("[Server/TCP] \"" + currentUser.getUsername() + "\" hat sich abgemeldet!");

        writer.println();
        writer.println("Sie werden nun vom Server abgemeldet!");
        writer.flush();

        currentUser.setWriter(null);
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
