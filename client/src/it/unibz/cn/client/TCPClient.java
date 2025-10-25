package it.unibz.cn.client;

import java.net.*;
import java.util.Scanner;

import java.io.*;

public class TCPClient {

    // single shared Scanner for System.in: do NOT close it (closing would close
    // System.in)
    private static final Scanner STDIN = new Scanner(System.in);
    // signal the receiver thread to stop
    private static volatile boolean serverReciverRunning = true;

    public static void main(String args[]) {
        String nickname = null;
        String host = (args.length >= 2) ? args[1] : "localhost";
        int serverPort = (args.length >= 3) ? Integer.parseInt(args[2]) : 7896;

        try (
                Socket s = new Socket(host, serverPort);
                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());) {
            // get nickname
            nickname = getNickname();

            // start tread to print messages from server
            Thread receiverThread = new Thread(() -> {
                try {
                    while (serverReciverRunning) {
                        String message = in.readUTF();
                        System.out.println("[Auction Server]: " + message);
                    }
                } catch (IOException e) {
                    if (serverReciverRunning) {
                        System.err.println("Error receiving message from auction server: " + e.getMessage());
                    }
                } finally {
                    serverReciverRunning = false;
                }
            });
            receiverThread.start();

            while (true) {
                String message = getMessage();

                // Exit condition
                if (message.equals("exit")) {
                    serverReciverRunning = false;
                    break;
                }

                message = nickname + ": " + message;

                out.writeUTF(message);
                out.flush();

            }

            try {
                receiverThread.join(1000); // Wait up to 1 second for the thread to terminate
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); 
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Sock: " + e.getMessage());
        } catch (EOFException e) {
            System.err.println("EOF: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO: " + e.getMessage());
        }
    }

    static String getNickname() {
        String nickname = null;

        System.out.println("Enter your nickname: ");
        nickname = STDIN.nextLine();
        while (nickname == null || nickname.trim().isEmpty() || !nickname.matches("[A-Za-z0-9_]{3,16}")) {
            System.out.println(
                    "Invalid nickname. It must be 3-16 characters and contain only letters, digits or underscores.");
            System.out.print("Enter your nickname: ");
            nickname = STDIN.nextLine();
        }

        return nickname;
    }

    static String getMessage() {
        String message = null;

        System.out.println("Enter your message: ");
        message = STDIN.nextLine();
        while (message == null || message.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            System.out.print("Enter your message: ");
            message = STDIN.nextLine();
        }

        return message;
    }

}
