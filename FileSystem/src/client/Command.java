package client;

import shared.Account;
import java.rmi.RemoteException;
import shared.AuthServerInterface;
import shared.FileServerInterface;

public abstract class Command{

    public abstract void run(Account account, FileServerInterface fileServer, String fileName) throws RemoteException;

}