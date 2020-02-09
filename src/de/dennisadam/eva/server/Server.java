package de.dennisadam.eva.server;

import de.dennisadam.eva.user.User;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static List<User> userList;
    private static int port;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(20);

        //Abfragen der Programmargumente und setzen des Ports
        if(args.length == 0){
            System.err.println("Usage: java -cp Server <listening port>");
            System.exit(0);
        } else {
            port = Integer.parseInt(args[0]);
        }

        //TODO: Auslesen der Benutzerliste aus JSON-Datei und schreiben in userList
        userList = new ArrayList<>();

        //Starten des Servers
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server erfolgreich gestartet!");
            System.out.println("Der Server ist nun auf dem Port \"" + port + "\" erreichbar...");

            //Listening starten (Server hört/wartet auf Anfragen)
            while(true){
                try{
                    System.out.println("[Server/TCP] Warte auf eingehende Verbindung...");
                    Socket client = serverSocket.accept();
                    System.out.println("[Server/TCP] Client verbunden: " + client.getRemoteSocketAddress());

                    executor.submit(new ServerHandlerThread(client));
                } catch (IOException e){
                    System.err.println("Verbindung zum Client konnte nicht aufgebaut werden oder wurde unerwartet beendet!");
//                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Server konnte nicht gestartet werden!");

            if(e instanceof BindException){
                System.err.println("Es läuft bereits ein Server auf Port " + port + "!");
            }
//            e.printStackTrace();
        }
    }

    public static User userListContains(String username) {
        for(User u : userList){
            if(u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

}
