package de.dennisadam.eva.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String hostname = "localhost";

        System.out.println("[Client] Verbindung wird aufgebaut...");

        try(
            Socket socket = new Socket(hostname, 1608);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            Scanner consoleIn = new Scanner(System.in);
            Scanner receiver = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())))
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

            //Hier bekommen wird als erstes die Liste der verfügbaren Befehle, das erste Mal angezeigt
            if(receiver.hasNext()){
                System.out.println(receiver.nextLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
