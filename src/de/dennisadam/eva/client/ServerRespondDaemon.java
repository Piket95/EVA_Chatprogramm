package de.dennisadam.eva.client;

import java.io.BufferedReader;
import java.io.IOException;

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
                System.out.println(line);

                if(line.contains("abgemeldet!")){
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
