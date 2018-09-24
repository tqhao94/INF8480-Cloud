package server;

import java.io.File;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import shared.Account;
import shared.AuthServerInterface;
import shared.FileServerInterface;

public class FileServer implements FileServerInterface {
	private static final String FILES_DIR_NAME = "files";

	public static void main(String[] args) {
		File filesDir = new File(FILES_DIR_NAME);
		filesDir.mkdir();
		FileServer server = new FileServer();
		server.run();
	}

	private AuthServerInterface authServer = null;

	public FileServer() {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		authServer = loadAuthServer("127.0.0.1");
	}

	private AuthServerInterface loadAuthServer(String hostname) {
		AuthServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (AuthServerInterface) registry.lookup("authServer");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			FileServerInterface stub = (FileServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("fileServer", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	
	/*
	 * Méthodes accessibles par RMI. 
	 */
	 
	@Override
	public void createFile(Account account, String fileName) throws RemoteException {
		if(!authServer.verifyAccount(account)) throw new RemoteException("Ce compte n'existe pas ou le mot de passe est invalide");
		return ;
	}
	
	@Override
	public String[] listFiles(Account account) throws RemoteException {
		if(!authServer.verifyAccount(account)) throw new RemoteException("Ce compte n'existe pas ou le mot de passe est invalide");
		return "1 2 3 4 5".split(" ");
	}
	
	@Override
	public byte[] getFile(Account account, String name, String checksum) throws RemoteException {
		if(!authServer.verifyAccount(account)) throw new RemoteException("Ce compte n'existe pas ou le mot de passe est invalide");
		return new byte[5];
	}

	@Override
	public String lockFile(Account account, String name, String checksum) throws RemoteException {
		if(!authServer.verifyAccount(account)) throw new RemoteException("Ce compte n'existe pas ou le mot de passe est invalide");
		return "";
	}

	@Override
	public String pushFile(Account account, String name, byte[] fileContent)  throws RemoteException {
		if(!authServer.verifyAccount(account)) throw new RemoteException("Ce compte n'existe pas ou le mot de passe est invalide");
		return "";
	}

	@Override
	public byte[] syncLocalDirectory(Account account) throws RemoteException {
		if(!authServer.verifyAccount(account)) throw new RemoteException("Ce compte n'existe pas ou le mot de passe est invalide");
		return new byte[5];
	}
}
