package de.dennisadam.eva.server;

import de.dennisadam.eva.server.user.UserList;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static UserList userList;
    private static int port;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        //Abfragen der Programmargumente und setzen des Ports
        if(args.length == 0){
            System.err.println("Usage: java -jar Server.jar <listening port>");
            System.exit(0);
        } else {
            try{
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e){
                System.err.println("Das mitgelieferte Argument ist keine Zahl...");
                System.exit(0);
            }
        }

        //TODO: Auslesen der Benutzerliste aus JSON-Datei und schreiben in userList (eventuell nicht notwendig)
        userList = new UserList(new ArrayList<>());

        //Starten des Servers
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server erfolgreich gestartet!");
            System.out.println("Der Server ist nun auf dem Port \"" + serverSocket.getLocalPort() + "\" erreichbar...");

            //ServerTimout bis Verbindung zum Client gekappt wird
//            serverSocket.setSoTimeout(15*60000);

            //Listening starten (Server hört/wartet auf Anfragen)
            while(true){
                try{
                    System.out.println("[Server/TCP] Warte auf eingehende Verbindung...");
                    Socket client = serverSocket.accept();
                    System.out.println("[Server/TCP] Client verbunden: " + client.getInetAddress().toString().substring(client.getInetAddress().toString().lastIndexOf("/") + 1));

                    new Thread(new ClientHandler(client)).start();
                } catch (IOException e){
                    System.err.println("Verbindung zum Client konnte nicht aufgebaut werden oder wurde unerwartet beendet!");
                    System.err.println("Grund:");
                    e.getMessage();
                }
            }
        } catch (IOException e) {
            System.err.println("Server konnte nicht gestartet werden!");

            if(e instanceof BindException){
                System.err.println("Es läuft bereits ein Server auf Port " + port + "!");
            }
            else{
                System.err.println("Grund:");
                System.err.println(e.getMessage());
//                e.printStackTrace();
            }
        }
    }

}
