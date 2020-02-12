package de.dennisadam.eva.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

public class ServerRespondDaemon implements Runnable {

    BufferedReader reader;

    public ServerRespondDaemon(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        try{
            String line;
            while((line = reader.readLine()) != null){
                if(line.equals("EXIT")){
                    System.exit(0);
                }

                System.out.println(line);
            }
        } catch (IOException e) {
            if(e instanceof SocketException){
                //TODO: Retry implementieren?
                System.err.println("Die Verbindung zum Server wurde unerwartet beendet...");
                System.exit(0);
            }
            else {
                e.printStackTrace();
            }
        }
    }
}
