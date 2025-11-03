package common;

public class Commands {

    public static String getErrorCommand(){
        return "ERR";
    }

    public static String getClientCommand(EClientToServerCommands command){
        switch(command){
            case EXIT -> { return "EXIT"; }
            case STATUS -> { return "STATUS"; }
            case MESSAGE -> { return "MESSAGE"; }
            case BID -> { return "BID"; }
            case REGISTER -> { return "REGISTER"; }
            default -> { return "HELP"; }
        }
    }

    public static String getServerCommand(EServerToClientCommands command){
        switch(command){
            case USER_REGISTER_SUCCESS -> { return "USER_REGISTER_SUCCESS"; }
            case USER_REGISTER_FAIL -> { return "USER_REGISTER_FAIL"; }
            case IGNORE -> { return "IGNORE"; }
            default -> { return "PRINT_MESSAGE"; }
        }
    }
}
