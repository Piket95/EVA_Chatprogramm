package de.dennisadam.eva;

import java.util.Scanner;

public class Main {

    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welche Rolle uebernimmt dieser PC? (Bitte Nummer eingeben!)");
        System.out.println("1: Client");
        System.out.println("2: Server");
        System.out.println("3: Test");

        int role = Integer.parseInt(sc.nextLine());

        switch (role) {
            case 1:
                startClient();
                break;
            case 2:
                startServer();
                break;
            case 3:
                test();
                break;
            default:
                break;
        }

        sc.close();
    }

    private static void startClient(){
        System.out.println("Starting Client...");
    }

    private static void startServer(){
        Server server = new Server();
        System.out.println("[Server/TCP] Wird gestartet...");
        server.startListening();
    }

    private static void test(){
        System.out.println("Starting Test...");
    }
}