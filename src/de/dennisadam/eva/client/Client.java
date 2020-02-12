package de.dennisadam.eva.client;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        int PORT = 1608;
        String HOSTNAME = "localhost";

        //Abfragen der Programmargumente und setzen des Ports
        if(args.length != 2){
            System.err.println("Usage: java -jar Client.jar <ServerIP/Hostname> <listening port>");
            System.exit(0);
        } else {
            HOSTNAME = args[0];

            if(!(HOSTNAME.matches("^.+\\.[a-z]{2,}") || HOSTNAME.matches("^\\d{2,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$") || HOSTNAME.contains("localhost"))){
                System.err.println("Das 1. mitgelieferte Argument ist weder eine IP noch ein Hostname!");
                System.exit(0);
            }

            try{
                PORT = Integer.parseInt(args[1]);
            } catch (NumberFormatException e){
                System.err.println("Das mitgelieferte Portargument (Argument 2) ist keine Zahl!");
                System.exit(0);
            }
        }

        System.out.println("[Client] Verbindung zum Server \""+ HOSTNAME + ":" + PORT + "\" wird aufgebaut...");

        try(
            Socket socket = new Socket(HOSTNAME, PORT);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            Scanner consoleIn = new Scanner(System.in);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ){
            System.out.println("[Client] Verbindung zum Server (" + socket.getInetAddress().toString().substring(socket.getInetAddress().toString().lastIndexOf("/") + 1) + ":" +  socket.getPort() + ") erfolgreich aufgebaut...");

            Thread daemon = new Thread(new ServerRespondDaemon(reader));
            daemon.setDaemon(true);
            daemon.start();

            //Schleife, in der durchgängig (endlos) auf Eingaben des Nutzers gewartet wird
            while(true){
                writer.println(consoleIn.nextLine());
                writer.flush();
            }

        } catch (IOException e) {
            if(e instanceof ConnectException){
                System.err.println("Es konnte keine Verbindung zum Server (" + HOSTNAME + ":" + PORT + ") aufgebaut werden!");
                System.err.println("Bitte überprüfe IP/Hostname bzw. den Port!");
            }
            else if(e instanceof SocketException){
                System.err.println("Die Verbindung zum Server wurde unerwartet unterbrochen...");
                e.printStackTrace();
            }
            else{
                e.printStackTrace();
            }
        }
    }
}
