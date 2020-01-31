package de.dennisadam.eva.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {

        System.out.println("Starte TCP Listening auf Port 1608!");

        while(true){
            try(ServerSocket serverSocket = new ServerSocket(1608)){
                System.out.println("[Server/TCP] Warte auf eingehende Verbindung...");
                Socket client = serverSocket.accept();
                System.out.println("[Server/TCP] Client verbunden: " + client.getRemoteSocketAddress());

                Scanner serverScanner = new Scanner(new BufferedReader(new InputStreamReader(client.getInputStream())));
                PrintWriter writer = new PrintWriter(client.getOutputStream());

                if(serverScanner.hasNext()){
                    System.out.println("[Server/TCP] Empfangen von Client (" + client.getRemoteSocketAddress() + "): " + serverScanner.nextLine());
                }

                serverScanner.close();
                writer.close();

                client.close();
                System.out.println("[Server/TCP] Verbindung zum Client abgebaut!");

                //Server wartet hier nun wieder auf eine neu eingehende Verbindung
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
