package de.dennisadam.eva.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

public class ServerRespondDaemon implements Runnable {

    final BufferedReader reader;
    final String hostname;
    final int port;

    public ServerRespondDaemon(String hostname, int port, BufferedReader reader) {
        this.hostname = hostname;
        this.port = port;
        this.reader = reader;
    }

    @Override
    public void run() {
        try{
            String line;
            while((line = reader.readLine()) != null){
                if(line.equals("EXIT")){
                    System.out.println("Programm wird beendet...");
                    System.exit(0);
                }

                System.out.println(line);
            }

            System.err.println("[Client] Verbindung zum Server \"" + hostname + ":" + port + "\" verloren...");
            System.out.println("Programm wird beendet...");
            System.exit(0);
        } catch (IOException e) {
            if(e instanceof SocketException){
                System.err.println("Die Verbindung zum Server wurde unerwartet beendet...");
                System.exit(0);
            }
            else {
                e.printStackTrace();
            }
        }
    }
}
