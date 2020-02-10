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
                if(line.equals("SHUTDOWN")){
                    System.exit(0);
                }

                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
