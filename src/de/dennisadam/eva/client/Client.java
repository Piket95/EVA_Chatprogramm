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

            System.out.println("Warst du schonmal angemeldet? (j/n)");

            if(consoleIn.nextLine().equals("j")){
                System.out.println("Bitte gib den Benutzernamen ein, den du das letzte Mal verwendet hast: (Achtung: case sensitive)");
            }
            else{
                System.out.println("Unter welchem Namen möchtest du für andere Sichtbar sein?");
            }

            writer.println(consoleIn.nextLine());
            writer.flush();

            //Hier bekommen wird als erstes die Liste der verfügbaren Befehle, das erste Mal angezeigt und dass man sich erfolgreich angemeldet hat
            //STOP ist hier der letzte Eintrag und das STOP-Token, dass daraufhin nix mehr kommt!
            String line;
            while(!(line = reader.readLine()).contains("STOP")){
                System.out.println(line);
            }

            //Schleife, in der Abwechselnd Befehle gesandt werden und auf die Antwort des Servers gewartet wird
            while(true){
                System.out.println("Befehl:");
                writer.println(consoleIn.nextLine());
                writer.flush();

                while(!(line = reader.readLine()).contains("STOP")){
                    System.out.println(line);

                    if(line.contains("abgemeldet!")){
                        socket.close();
                        System.exit(0);
                    }
                }
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
