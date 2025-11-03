package client;

import common.Commands;
import common.EClientToServerCommands;
import common.EServerToClientCommands;
import common.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import static common.EClientToServerCommands.*;
import static common.EServerToClientCommands.*;

public class TCPClient {

    static boolean userSet = false;

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

            // Loop
            while (true) {

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

                /*
                if (!scanner.hasNextLine()) { // EOF (Ctrl+D/Ctrl+Z)
                    System.out.println("\nInput invalid. Closing connection");
                    break;
                }
                 */


                if (inMessage.type.equalsIgnoreCase(Commands.getClientCommand(EXIT))) {
                    System.out.println("User is closing connection. Bye!");
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
                String reply = in.readUTF();

                processServerResponse(reply);
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

    public static Message processUserInput(String userInput) {

        Message message = new Message();

        String[] parts = userInput.split(" ", 2);
        String inCommand = parts[0];
        String inArgument = parts.length > 1 ? parts[1] : "";

        // command exit
        if (inCommand.equalsIgnoreCase(Commands.getClientCommand(EXIT))) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = Commands.getClientCommand(EXIT);
                message.content = "";
            }

            // command help
        } else if (inCommand.equalsIgnoreCase(Commands.getClientCommand(HELP))) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = Commands.getClientCommand(HELP);
                message.content = "";
            }

            // command status
        } else if (inCommand.equalsIgnoreCase(Commands.getClientCommand(STATUS))) {
            if(!inArgument.isEmpty()){
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = Commands.getClientCommand(STATUS);
                message.content = "";
            }

            // command message
        } else if (inCommand.equalsIgnoreCase(Commands.getClientCommand(MESSAGE))) {
            if(!inArgument.isEmpty()){
                message.type = Commands.getClientCommand(MESSAGE);
                message.content = inArgument;
            } else {
                message.type = Commands.getErrorCommand();
                message.content = "";
            }

            // command bid
        } else if (inCommand.equalsIgnoreCase(Commands.getClientCommand(BID))) {
            if(isStringNumeric(inArgument)){
                message.type = Commands.getClientCommand(BID);
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
        if (inCommand.equalsIgnoreCase(Commands.getClientCommand(EXIT))) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = Commands.getClientCommand(EXIT);
                message.content = "";
            }

            // command help
        } else if (inCommand.equalsIgnoreCase(Commands.getClientCommand(HELP))) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = Commands.getClientCommand(HELP);
                message.content = "";
            }

            // command username
        } else {
            if (!isUsernameValid(inCommand)) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = Commands.getClientCommand(REGISTER);
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
        } else if (message.type.equalsIgnoreCase(Commands.getClientCommand(HELP))) {
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
        } else if (message.type.equalsIgnoreCase(Commands.getClientCommand(HELP))) {
            System.out.println(getHelpMessagePreRegister());

            // returning the empty string, results in the client
            // not sending anything to the server
            return "";
        } else if (message.type.equalsIgnoreCase(Commands.getClientCommand(REGISTER))) {
            return message.encode();
        }

        return "";
    }

    public static void processServerResponse(String serverInput){
        Message message = new Message();
        message.decode(serverInput);

        if(message.type.equals(Commands.getServerCommand(USER_REGISTER_SUCCESS))){
            System.out.println("User registered successfully.");
            userSet = true;
        } else if (message.type.equals(Commands.getServerCommand(USER_REGISTER_FAIL))){
            System.out.println("Username already in use.");
        } else if (message.type.equals(Commands.getServerCommand(PRINT_MESSAGE))){
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

    public static void processServerReply(String inString) {
        Message message = new Message();
        message.decode(inString);
    }
}
