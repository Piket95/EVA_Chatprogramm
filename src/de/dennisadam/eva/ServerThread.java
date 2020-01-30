package de.dennisadam.eva;

import java.net.Socket;

public class ServerThread implements Runnable {

    private Socket client;

    public ServerThread(Socket client){
        this.client = client;
    }

    @Override
    public void run() {

    }
}
