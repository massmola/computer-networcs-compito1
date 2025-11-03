package client;

import common.Commands;
import common.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.lang.Thread;

import static common.EClientToServerCommands.*;
import static common.EServerToClientCommands.*;

public class TCPClient {

    static volatile boolean userSet = false;
    static volatile boolean serverReciverRunning = true;

    public static void main (String[] args) {
        int serverPort = 7896;
        String serverAddress = "localhost"; // oppure un IP/hostname
        Scanner scanner = null;

        try (
            Socket clientSocket = new Socket(serverAddress, serverPort);
            DataInputStream in  = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            )
            {
                
            Thread receiverThread = new Thread(() -> reciveMessagesFromServer(in));
            receiverThread.start();

            // Read from keyboard
            scanner = new Scanner(System.in);

            // Connection Established
            System.out.println("Connection established with " + serverAddress + ":" + serverPort);

            // Loop
            while (serverReciverRunning) {

                String outMessage = "";
                Message inMessage = new Message();

                // Take user input
                String inString = "";

                if(userSet){
                    System.out.println("Enter a command:");
                    inString = scanner.nextLine().trim();

                    inMessage = processUserInput(inString);
                    outMessage = processInputMessage(inMessage);
                } else {
                    System.out.println("Enter a username:");
                    inString = scanner.nextLine().trim();

                    inMessage = processUserInputPreRegister(inString);
                    outMessage = processInputMessagePreRegister(inMessage);
                }

                if (inMessage.type.equalsIgnoreCase(EXIT.name())) {
                    System.out.println("User is closing connection. Bye!");
                    serverReciverRunning = false;
                    break; // DO NOT send anything to the server
                }

                if (inString.isEmpty() || outMessage.isEmpty()) {
                    // Avoid sending empty strings
                    continue;
                }

                if (inMessage.type.equalsIgnoreCase(Commands.getErrorCommand())){
                    continue;
                }

                // Send and Receive
                out.writeUTF(outMessage);
                out.flush();
            }

        } catch (UnknownHostException e) {
            System.out.println("Sock: " + e.getMessage());

        } catch (EOFException e) {
            System.out.println("EOF: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());

        } finally {
            if (scanner != null) {
                scanner.close();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            } // Give some time for the receiver thread to finish
            serverReciverRunning = false;
        }
    }

    public static Message processUserInput(String userInput) {

        Message message = new Message();

        String[] parts = userInput.split(" ", 2);
        String inCommand = parts[0];
        String inArgument = parts.length > 1 ? parts[1] : "";

        // command exit
        if (inCommand.equalsIgnoreCase(EXIT.name())) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = EXIT.name();
                message.content = "";
            }

            // command help
        } else if (inCommand.equalsIgnoreCase(HELP.name())) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = HELP.name();
                message.content = "";
            }

            // command status
        } else if (inCommand.equalsIgnoreCase(STATUS.name())) {
            if(!inArgument.isEmpty()){
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = STATUS.name();
                message.content = "";
            }

            // command message
        } else if (inCommand.equalsIgnoreCase(MESSAGE.name())) {
            if(!inArgument.isEmpty()){
                message.type = MESSAGE.name();
                message.content = inArgument;
            } else {
                message.type = Commands.getErrorCommand();
                message.content = "";
            }

            // command bid
        } else if (inCommand.equalsIgnoreCase(BID.name())) {
            if(isStringNumeric(inArgument)){
                message.type = BID.name();
                message.content = inArgument;
            } else {
                message.type = Commands.getErrorCommand();
                message.content = "";
            }

            // none
        } else {
            message.type = Commands.getErrorCommand();
            message.content = "";
        }

        return message;
    }

    public static Message processUserInputPreRegister(String userInput){

        Message message = new Message();

        String[] parts = userInput.split(" ", 2);
        String inCommand = parts[0];
        String inArgument = parts.length > 1 ? parts[1] : "";

        // command exit
        if (inCommand.equalsIgnoreCase(EXIT.name())) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = EXIT.name();
                message.content = "";
            }

            // command help
        } else if (inCommand.equalsIgnoreCase(HELP.name())) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = HELP.name();
                message.content = "";
            }

            // command username
        } else {
            if (!isUsernameValid(inCommand)) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = REGISTER.name();
                message.content = inCommand;
            }
        }

        return message;
    }

    public static String processInputMessage(Message message){
        if (message.type.equalsIgnoreCase(Commands.getErrorCommand())) {
            System.out.println(getErrorMessage());
            System.out.println(getHelpMessage());

            // returning the empty string, results in the client
            // not sending anything to the server
            return "";
        } else if (message.type.equalsIgnoreCase(HELP.name())) {
            System.out.println(getHelpMessage());

            // returning the empty string, results in the client
            // not sending anything to the server
            return "";
        }

        return message.encode();
    }

    public static String processInputMessagePreRegister(Message message){
        if (message.type.equalsIgnoreCase(Commands.getErrorCommand())) {
            System.out.println(getErrorMessage());
            System.out.println(getHelpMessagePreRegister());

            // returning the empty string, results in the client
            // not sending anything to the server
            return "";
        } else if (message.type.equalsIgnoreCase(HELP.name())) {
            System.out.println(getHelpMessagePreRegister());

            // returning the empty string, results in the client
            // not sending anything to the server
            return "";
        } else if (message.type.equalsIgnoreCase(REGISTER.name())) {
            return message.encode();
        }

        return "";
    }

    public static void processServerResponse(String serverInput){
        Message message = new Message();
        message.decode(serverInput);

        if(message.type.equals(USER_REGISTER_SUCCESS.name())){
            System.out.println("User registered successfully.");
            userSet = true;
        } else if (message.type.equals(USER_REGISTER_FAIL.name())){
            System.out.println("Username already in use.");
        } else if (message.type.equals(PRINT_MESSAGE.name())){
            System.out.println(message.content);
        } else {
            // Case: IGNORE
            return;
        }
    }

    public static String getHelpMessage() {
        return "Enter one of the following commands in this format:\n" +
                "\texit\t\t\t\t\t\t\tto quit the connection\n" +
                "\thelp\t\t\t\t\t\t\tto print an help message\n" +
                "\tstatus\t\t\t\t\t\tto print the status of the current bid\n" +
                "\tmessage messageContent\t\tto send a message to all the clients who are connected\n" +
                "\tbid bidAmount\t\t\t\tto place a bid and notify all connected clients\n";
    }

    public static String getHelpMessagePreRegister() {
        return "Enter one of the following commands in this format:\n" +
                "\texit\t\t\t\t\t\t\tto quit the connection\n" +
                "\thelp\t\t\t\t\t\t\tto print an help message\n" +
                "\t*Your Username*\t\t\t\tmust contain only letters, numbers and underscores. Must be within 3 and 16 characters long\n";
    }

    public static String getErrorMessage() {
        return "The entered message has an invalid format";
    }

    public static boolean isStringNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    public static boolean isUsernameValid(String inUsername){
        return !(inUsername == null || inUsername.trim().isEmpty() || !inUsername.matches("[A-Za-z0-9_]{3,16}"));
    }

    private static void reciveMessagesFromServer(DataInputStream in) {
        try {
            while (serverReciverRunning) {
                String rawMessage = in.readUTF();
                processServerResponse(rawMessage);
            }
        } catch (IOException e) {
            if (serverReciverRunning) {
                System.err.println("Error receiving message from auction server: " + e.getMessage());
            }
        } finally {
            serverReciverRunning = false;
        }
    }
}
