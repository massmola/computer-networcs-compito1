import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInteraction extends Remote {
    String RMIMethod() throws RemoteException;
}
