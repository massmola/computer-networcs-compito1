package common;

public class Commands {

    public static String getErrorCommand(){
        return "ERR";
    }

    public static String getClientCommand(EClientToServerCommands command){
        return command.name();
    }

    public static String getServerCommand(EServerToClientCommands command){
        return command.name();
    }
}
