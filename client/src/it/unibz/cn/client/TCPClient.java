package it.unibz.cn.client;

import java.net.*;
import java.util.Scanner;

import javax.swing.plaf.TreeUI;

import java.io.*;

public class TCPClient {

    public static void main(String args[]) {
        String nickname = null;
        String host = (args.length >= 2) ? args[1] : "localhost";
        int serverPort = (args.length >= 3) ? Integer.parseInt(args[2]) : 7896;
        
        if (args.length < 2) {
            System.err.println("Usage: java it.unibz.cn.client.TCPClient <host> [port]");
            System.exit(2);
            
        }
        
        try (
            Socket s = new Socket(host, serverPort);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
        ) {
            nickname = getNickname();
            
            while (true) {
                String message = getMessage();

                // Exit condition
                if (message.equals("exit"))  break;

                message = nickname + ": " + message;
                
                out.writeUTF(message); 
                out.flush();


                String data = in.readUTF();
                System.out.println("Received: " + data);
            }
            
            s.close();
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
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your nickname: ");
        String nickname = scanner.nextLine();
        while (nickname == null || nickname.trim().isEmpty() || !nickname.matches("[A-Za-z0-9_]{3,16}")) {
            System.out.println("Invalid nickname. It must be 3-16 characters and contain only letters, digits or underscores.");
            System.out.print("Enter your nickname: ");
            nickname = scanner.nextLine();
        }
        scanner.close();
        return nickname;
    }

    static String getMessage() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your message: ");
        String message = scanner.nextLine();
        while (message == null || message.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            System.out.print("Enter your message: ");
            message = scanner.nextLine();
        }
        scanner.close();
        return message;
    }

}
