package de.dennisadam.eva.server;

import de.dennisadam.eva.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

public class ServerHandlerThread implements Runnable {

    private Socket client;
    private String username;
    private User currentUser;

    private boolean clientConnected;

    private PrintWriter writer;
    private String line;

    public ServerHandlerThread(Socket client){
        this.client = client;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()))){

            writer = new PrintWriter(client.getOutputStream());
            clientConnected = true;

            //Abfrage, ob username schon in Liste existiert oder dieser komplett neu angelegt werden muss
            if((line = reader.readLine()) != null){
                username = line;
                currentUser = Server.userListContains(username);

                if(currentUser == null){
                    currentUser = new User(username);
                    Server.userList.add(currentUser);
                }
                else {
                    currentUser.setOnline(true);
                }

                System.out.println("[Server/TCP] \"" + username + "\" (" + client.getRemoteSocketAddress() + ") erfolgreich angemeldet!");
                writer.println("\"" + username + "\" erfolgreich angemeldet!\n");
            }

            printCommandList();

            //Warte auf Befehle
            while((line = reader.readLine()) != null){

                if(line.contains("/chat")){
                    //TODO: Chatfunktion implementieren:
                    // - Client A kommuniziert mit Server-Thread A
                    // - Server-Thread A kommuniziert mit Server-Thread B
                    // - Server-Thread B kommuniziert mit Client B
                }
                else{
                    switch (line){
//                    case "":
//                        break;
                        case "/help":
                            printCommandList();
                            break;
                        case "/list":
                            writer.println("Liste aller Benutzer:");

                            for(User u : Server.userList){

                                if(u.isOnline()){
                                    writer.println(u.getUsername() + "(online)");
                                }
                                else{
                                    writer.println(u.getUsername());
                                }
                            }
                            writer.println("STOP");
                            writer.flush();
                            break;
                        case "/logout":
                            disconnectClient();
                            break;
                        default:
                            writer.println("Der Befehl existiert nicht! Vielleicht hast du dich vertippt?");
                            writer.flush();
                    }
                }

                if(!clientConnected){
                    break;
                }
            }

        } catch (IOException e) {
            if(e instanceof SocketException){
                currentUser.setOnline(false);
                System.err.println("Die Verbindung zum Client wurde unerwartet beendet!");
            }
            else{
                e.printStackTrace();
            }
        } finally{
            writer.close();
        }
    }

    public void printCommandList(){
        writer.println("------------------------Liste der Befehle------------------------");
        writer.println("/help\t\t\t\t\t\tZeigt die Liste der verfügbaren Befehle (diese hier)");
        writer.println("/list\t\t\t\t\t\tZeigt eine liste der verfügbaren Benutzer an");
        writer.println("/chat <Benutzername>\t\tStarte einen Chat mit dem angegebenen Benutzer (!case-sensitive)");
        writer.println("/logout\t\t\t\t\t\tMeldet dich vom Server ab und trennt die Verbindung!");
        writer.println("STOP");
        writer.flush();
    }

    public void disconnectClient(){

        try {
            writer.println("Sie werden nun in wenigen Sekunden vom Server abgemeldet!");
            writer.flush();
            TimeUnit.SECONDS.sleep(3);

            currentUser.setOnline(false);
            clientConnected = false;

            client.close();
        } catch (IOException | InterruptedException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt();
            }
            else {
                e.printStackTrace();
            }
        }

        System.out.println("[Server/TCP] Verbindung zum Client abgebaut!");
    }
}
