package de.dennisadam.eva.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ServerHandlerThread implements Runnable {

    private Socket client;
    private String username;

    public ServerHandlerThread(Socket client){
        this.client = client;
    }

    @Override
    public void run() {
        try(
            Scanner serverScanner = new Scanner(new BufferedReader(new InputStreamReader(client.getInputStream())));
            PrintWriter writer = new PrintWriter(client.getOutputStream())
        ){

            if(serverScanner.hasNext()){
                System.out.println("[Server/TCP] Gewählter Benutzername von Client (" + client.getRemoteSocketAddress() + "): " + serverScanner.nextLine());

                writer.println("Nachricht vom Server erfolgreich empfangen!\n");
                writer.flush();
            }

            writer.println("------------------------Liste der Befehle------------------------");
            writer.println("/help\t\t\tZeigt die Liste der verfügbaren Befehle (diese hier)");
            writer.println("/list\t\t\tZeigt eine liste der verfügbaren Benutzer an");
            writer.println("/chat <Benutzername>\t\t\tStarte einen Chat mit dem angegebenen Benutzer");
            writer.flush();

            TimeUnit.SECONDS.sleep(1); //Wird das hier vergessen, wird der Writer geschlossen und der Socket auch. Damit kann die letzte Nachricht vom Client nicht mehr empfangen werden.

            client.close();
            System.out.println("[Server/TCP] Verbindung zum Client abgebaut!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
