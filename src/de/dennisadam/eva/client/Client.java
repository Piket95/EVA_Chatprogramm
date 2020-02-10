package de.dennisadam.eva.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        int PORT = 1608;
        String HOSTNAME = "localhost";

        //TODO: Abfrage ob Port wirklich Port Format hat und keine Buchstaben beinhaltet und Hostname eine IP oder hostname ist
        //Abfragen der Programmargumente und setzen des Ports
        if(args.length != 2){
            System.err.println("Usage: java -cp Server <ServerIP/Hostname> <listening port>");
            System.exit(0);
        } else {
            HOSTNAME = args[0];
            PORT = Integer.parseInt(args[1]);
        }

        System.out.println("[Client] Verbindung wird aufgebaut...");

        try(
            Socket socket = new Socket(HOSTNAME, PORT);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            Scanner consoleIn = new Scanner(System.in);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ){
            System.out.println("[Client] Verbindung zum Server erfolgreich aufgebaut...");

            Thread daemon = new Thread(new ServerRespondDaemon(reader));
            daemon.setDaemon(true);
            daemon.start();

            //Schleife, in der durchgängig (endlos) auf Eingaben des Nutzers gewartet wird
            while(true){
                writer.println(consoleIn.nextLine());
                writer.flush();
            }

        } catch (IOException e) {
            if(e instanceof SocketException){
                System.err.println("Die Verbindung zum Server wurde unerwartet unterbrochen...");
            }
            else{
                e.printStackTrace();
            }
        }
    }
}
