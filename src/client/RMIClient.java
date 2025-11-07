package client;

import common.Commands;
import common.EClientToServerCommands;
import common.Message;
import server.ServerInterface;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import static common.EClientToServerCommands.*;
import static common.EClientToServerCommands.BID;

public class RMIClient extends UnicastRemoteObject implements ClientInterface {

    static ServerInterface server;
    static ClientInterface client;
    static String username = "";
    static boolean userSet = false;
    static boolean quitting = false;

    public static void main(String[] args) {

        try {
            // Lookup the remote object from the registry
            server = (ServerInterface) Naming.lookup("rmi://localhost:1099/RMI");
            client = new RMIClient();

            System.out.println("Connection with the server established.");

        } catch (NotBoundException e) {
            System.err.println("Not bound exception: " + e);
        } catch (RemoteException e) {
            System.err.println("Remote Exception: " + e);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL Exception: " + e);
        }

        menuLoop();

        System.exit(0);
    }

    private RMIClient() throws RemoteException {
        super();

        // Calls register (server function that registers client into the server)
        if(server != null){
            server.registerClient(this);
        }
    }

    // ------- Methods to handle user inputs -------

    private static void menuLoop() {

        Scanner scanner = new Scanner(System.in);

        while(!userSet && !quitting) {
            System.out.println("Enter a username: ");
            String in = scanner.nextLine();
            processUserInputPreRegister(in);
        }

        while(!quitting) {
            System.out.println("Enter a valid command: ");
            String in = scanner.nextLine();
            processUserInputPostRegister(in);
        }
    }

    // Before the client registers
    private static void processUserInputPreRegister(String in) {

        // If the client is not registered

        if (in.equalsIgnoreCase("/help")) {
            System.out.println(getHelpMessagePreRegister());
        } else if (in.equalsIgnoreCase("/quit")) {
            quit();
        } else if (isUsernameValid(in)) {
            try{
                userSet = server.registerUser(in);
                username = in;
                System.out.println("Registration successful!");
            } catch (RemoteException e){
                System.out.println("Remote exception: " + e);
            }
        } else {
            System.out.println(getErrorMessage() + getHelpMessagePreRegister());
        }
    }


    // After the client registers
    private static void processUserInputPostRegister(String in) {

        in = in.toLowerCase();
        Message message = new Message();

        String[] parts = in.split(" ", 2);
        String inCommand = parts[0];
        String inArgument = parts.length > 1 ? parts[1] : "";

        // command exit
        if (inCommand.equalsIgnoreCase("/" + QUIT.name())) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = QUIT.name();
                message.content = "";
            }

            // command help
        } else if (inCommand.equalsIgnoreCase("/" + HELP.name())) {
            if (!inArgument.isEmpty()) {
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = HELP.name();
                message.content = "";
            }

            // command status
        } else if (inCommand.equalsIgnoreCase("/" + STATUS.name())) {
            if(!inArgument.isEmpty()){
                message.type = Commands.getErrorCommand();
                message.content = "";
            } else {
                message.type = STATUS.name();
                message.content = "";
            }

            // command message
        } else if (inCommand.equalsIgnoreCase("/" + MESSAGE.name())) {
            if(!inArgument.isEmpty()){
                message.type = MESSAGE.name();
                message.content = inArgument;
            } else {
                message.type = Commands.getErrorCommand();
                message.content = "";
            }

            // command bid
        } else if (inCommand.equalsIgnoreCase("/" + BID.name())) {
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

        in = message.encode();

        if(!Message.isValidFormat(in)){
            System.out.println(getErrorMessage() + "\n" + getHelpMessage());
            return;
        }

        EClientToServerCommands commandType;
        try{
            commandType = EClientToServerCommands.valueOf(message.type);
        } catch (IllegalArgumentException e){
            System.out.println(getErrorMessage() + "\n" + getHelpMessage());
            return;
        }

        switch(commandType) {
            case QUIT: {
                quit();
                break;
            }
            case HELP: {
                System.out.println(getHelpMessage());
                break;
            }
            case STATUS: {
                try{
                    System.out.println(server.getStatus());
                } catch (RemoteException e){
                    System.out.println("Remote exception: " + e);
                }
                break;
            }
            case MESSAGE: {
                try{
                    server.sendMessage(username, message.content);
                } catch (RemoteException e){
                    System.out.println("Remote exception: " + e);
                }
                break;
            }
            case BID: {
                if(!isStringNumeric(message.content)){
                    System.out.println(getErrorMessage() + getHelpMessage());
                    break;
                }

                try{
                    if(!server.placeBid(username, Double.parseDouble(message.content))){
                        System.out.println(
                                "The input bid was refused.\n" +
                                "The bid must be a value higher than the current bid's value\n" +
                                "and a valid multiple of the item's minimum increment.");
                    }
                } catch (RemoteException e){
                    System.out.println("Remote exception: " + e);
                }
                break;

            }
            default: {
                System.out.println(getErrorMessage() + "\n" + getHelpMessage());
            }
        }
    }

    private static String getHelpMessage() {
        return "Enter one of the following commands in this format:\n" +
                "\t/quit\t\t\t\t\t\t\tto quit the connection\n" +
                "\t/help\t\t\t\t\t\t\tto print an help message\n" +
                "\t/status\t\t\t\t\t\t\tto print the status of the current bid\n" +
                "\t/message *messageContent*\t\tto send a message to all the clients who are connected\n" +
                "\t/bid *bidAmount*\t\t\t\tto place a bid and notify all connected clients\n";
    }

    private static String getHelpMessagePreRegister() {
        return "Enter one of the following commands in this format:\n" +
                "\t/quit\t\t\t\t\tto quit the connection\n" +
                "\t/help\t\t\t\t\tto print an help message\n" +
                "\t*Your Username*\t\t\tmust contain only letters, numbers and underscores. Must be within 3 and 16 characters long\n";
    }

    private static String getErrorMessage() {
        return "The entered message has an invalid format";
    }

    private static boolean isStringNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    private static boolean isUsernameValid(String inUsername){
        return !(inUsername == null || inUsername.trim().isEmpty() || !inUsername.matches("[A-Za-z0-9_]{3,16}"));
    }

    // ------- Quit -------

    /**
     * Removes the client from the server's client list and sets
     * quitting to true, exiting the application's loop.
     */
    private static void quit(){
        try{
            server.removeClient(client);
        } catch (RemoteException e){
            System.out.println("Remote exception: " + e);
        }
        quitting = true;
    }

    // ------- RMI Client Methods -------

    @Override
    public void receiveMessage(String in) throws RemoteException {
        System.out.println(in);
    }

}
