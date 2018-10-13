package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

//Interface pour les fonctions du serveur de calculs

public interface CalculationServerInterface extends Remote, Comparable<CalculationServerInterface> {
    public int calculateOperations(List<OperationTodo> operation) throws RemoteException;
    public int getCapacity();
}
