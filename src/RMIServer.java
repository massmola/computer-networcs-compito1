import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements RMIInteraction {

    protected RMIServer() throws RemoteException {
        super();
    }

    @Override
    public String RMIMethod() throws RemoteException {
        return "Hello from the RMI Server!";
    }

    public static void main(String[] args) {
        try {
            // Start the RMI registry on port 1099 (default)
            LocateRegistry.createRegistry(7896);
            System.out.println("RMI registry started.");

            // Create an instance of the server and bind it
            RMIServer server = new RMIServer();
            Naming.rebind("localhost", server);

            System.out.println("Server is ready.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }
}
