package de.dennisadam.eva.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String hostname = "localhost";

        System.out.println("[Client] Verbindung wird aufgebaut...");

        try(
            Socket socket = new Socket(hostname, 1608);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            Scanner consoleIn = new Scanner(System.in);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ){
            System.out.println("[Client] Verbindung zum Server erfolgreich aufgebaut...");

            System.out.println("Bitte gib deinen Benutzernamen ein: (Achtung: case sensitive)");

            writer.println(consoleIn.nextLine());
            writer.flush();

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
