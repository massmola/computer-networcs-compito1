package it.unibz.cn.client;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {

    public static void main (String[] args) {

        Socket clientSocket = null;
        Scanner scanner = null;

        try {
            int serverPort = 7896;
            String serverAddress = "localhost"; // oppure un IP/hostname

            clientSocket = new Socket(serverAddress, serverPort);

            DataInputStream in  = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            // Read from keyboard
            scanner = new Scanner(System.in);

            // Connection Established
            System.out.println("Connection established with " + serverAddress + ":" + serverPort);
            System.out.println("Enter a valid username: ");

            // Loop
            while (true) {

                System.out.print("> ");

                if (!scanner.hasNextLine()) { // EOF (Ctrl+D/Ctrl+Z)
                    System.out.println("\nInput invalid. Closing connection");
                    break;
                }

                // Take user input
                String inString = scanner.nextLine().trim();

                if (inString.equalsIgnoreCase("exit")) {
                    System.out.println("User is closing connection. Bye!");
                    break; // DO NOT send anything to the server
                }

                if (inString.isEmpty()) {
                    // Avoid sending empty strings
                    continue;
                }

                // Send and Receive
                out.writeUTF(inString);
                out.flush();
                String reply = in.readUTF();

                System.out.println("Server: " + reply);
            }

        } catch (UnknownHostException e) {
            System.out.println("Sock: " + e.getMessage());

        } catch (EOFException e) {
            System.out.println("EOF: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());

        } finally {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Close failed: " + e.getMessage());
                }
            }
        }
    }
}
