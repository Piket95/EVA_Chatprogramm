package de.dennisadam.eva.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String hostname = "localhost";
        InetSocketAddress address = new InetSocketAddress(hostname, 1608);

        System.out.println("[Client] Verbindung wird aufgebaut...");

        try(Socket socket = new Socket(hostname, 1608); PrintWriter writer = new PrintWriter(socket.getOutputStream())){
//            socket.connect(address);
            System.out.println("[Client] Verbindung zum Server erfolgreich aufgebaut...");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Was möchtest du schicken?");

            String message = scanner.nextLine();

            writer.println(message);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
