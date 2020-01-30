package de.dennisadam.eva;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port = 1608;

    private Thread tcpThread;
    private Runnable newClientThread;

    public void startListening(){
        System.out.println("Starte TCP Listening auf Port 1608!");

        tcpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try(ServerSocket serverSocket = new ServerSocket(port)){
                        System.out.println("[Server/TCP] Warte auf eingehende Verbindung...");
                        Socket client = serverSocket.accept();

                        System.out.println("[Server/TCP] Client verbunden: " + client.getRemoteSocketAddress());

                        newClientThread = new ServerThread(client); //Informationen des neu Verbundenden Clients werden an den neuen Thread übergeben. Dieser wird nach Abschluss die Verbindung kappen.

                        //Server wartet hier nun wieder auf eine neu eingehende Verbindung?!
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        tcpThread.start();
    }

}
