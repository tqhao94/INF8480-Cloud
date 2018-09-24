package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import shared.Account;
import shared.AuthServerInterface;
import shared.FileServerInterface;

public class Client {

	public static void main(String[] args) {
		String distantHostname = null;

		if (args.length < 1 || args.length > 2) {
			printHelp();
			return;
		}

		Command command = null;
		String fileName = "";
		// TODO: ajouter option de serveur distant
		try {
			switch (args[0]) {
			case "list":
				command = new ListCommand();
				break;
			case "create":
				command = new CreateCommand();
				fileName = args[1];
				break;
			case "get":
				command = new GetCommand();
				fileName = args[1];
				break;
			case "push":
				command = new PushCommand();
				fileName = args[1];
				break;
			case "lock":
				command = new LockCommand();
				fileName = args[1];
				break;
			case "syncLocalDirectory":
				command = new SyncCommand();
				break;
			default:
				System.err.println("Mauvaise commande : " + args[0]);
				printHelp();
				return;
			}
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Veuillez entrer un nom de fichier");
			return;
		}

		Client client = new Client(distantHostname);
		client.run(command, fileName);
	}

	public static void printHelp() {
		System.out.println("Liste des commandes :\n" + "list\n" + "create nomDeFichier\n" + "get nomDeFichier\n"
				+ "push nomDeFichier\n" + "lock nomDeFichier\n" + "syncLocalDirectory");
	}

	private AuthServerInterface authServer = null;
	private FileServerInterface fileServer = null;
	private Account account;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		authServer = loadAuthServer("127.0.0.1");
		fileServer = loadFileServer("127.0.0.1");
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

	private FileServerInterface loadFileServer(String hostname) {
		FileServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (FileServerInterface) registry.lookup("fileServer");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private void run(Command command, String fileName) {
		if (authServer != null) {
			try {
				checkExistingAccount();
				if (account.userName == null || account.password == null) {
					System.err.println("Votre fichier d'informations de compte n'a pas le format attendu.");
					return;
				}
				command.run(account, fileServer, fileName);
			} catch (RemoteException e) {
				System.err.println("Erreur: " + e.getMessage());
			}
		}
	}

	private void checkExistingAccount() {
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader("credentials"));
			try {
				String login = fileReader.readLine();
				String password = fileReader.readLine();
				account = new Account(login, password);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} finally {
				try {
					if (fileReader != null)
						fileReader.close();
				} catch (IOException e) {
					System.err.println("Un problème inconnu est survenu : " + e.getMessage());
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Vous n'avez pas de compte, entrer les identifiants désirés :");
			createAccount();
		}
	}

	private void createAccount() {
		Scanner reader = new Scanner(System.in);
		boolean validAccount = false;
		while (!validAccount) {
			String userName = "", pass = "";
			System.out.println("Nom d'utilisateur : ");
			boolean nameValid = false;
			while (!nameValid) {
				userName = reader.nextLine();
				if (userName.length() <= 1) {
					System.out.println("Entrez un nom plus long.");
				} else {
					nameValid = true;
				}
			}
			System.out.println("Mot de passe : ");
			boolean passValid = false;
			while (!passValid) {
				pass = reader.nextLine();
				if (pass.length() <= 2) {
					System.out.println("Entrez un mot de passe plus long.");
				} else {
					passValid = true;
				}
			}

			try {
				validAccount = authServer.newAccount(userName, pass);
				if (validAccount) {
					account = new Account(userName, pass);
					try (PrintStream ps = new PrintStream("credentials")) {
						ps.println(userName);
						ps.println(pass);
						System.out.println("Création du compte réussie!");
					} catch (FileNotFoundException e){
						System.err.println("Problème lors de la création du fichier d'informations de compte.");
						return ;
					}
				} else {
					System.out.println("Ce nom d'utilisateur n'est pas disponible, veuillez recommencer.");
				}
			} catch (RemoteException err) {
				System.err.println(
						"Erreur liée à la connexion au serveur. Abandon de la tentative de création de compte.");
				reader.close();
				return;
			}
		}

		reader.close();
	}
}