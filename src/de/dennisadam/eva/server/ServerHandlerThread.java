package de.dennisadam.eva.server;

import java.net.Socket;

public class ServerHandlerThread implements Runnable {

    private Socket client;

    public ServerHandlerThread(Socket client){
        this.client = client;
    }

    @Override
    public void run() {

    }
}
