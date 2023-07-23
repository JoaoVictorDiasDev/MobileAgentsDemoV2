package main.Servers;

import main.Interfaces.INameServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class NameServer extends UnicastRemoteObject implements INameServer {
    private final Map<String, String> map = new HashMap<>();

    protected NameServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        createNameServer();
    }

    @Override
    public String getAgencyByAgent(String agentName) {
        return map.get(agentName);
    }

    @Override
    public void associateAgentWithAgency(String agentName, String agencyName) throws RemoteException {
        map.put(agentName, agencyName);
    }

    @Override
    public void removeAgent(String agentName) throws RemoteException {
        map.remove(agentName);
    }

    @Override
    public Map<String, String> getMap() throws RemoteException {
        return map;
    }

    private static void createNameServer() {
        try
        {
            String message = "Criando servidor de nomes";
            String separator = "\u001B[1m" + "-".repeat(message.length());

            String reset = "\u001B[0m";  // Reset all formatting
            String bold = "\u001B[1m";   // Bold text
            String green = "\u001B[32m"; // Green text

            System.out.println(separator);
            System.out.println(bold + green + message + reset);
            System.out.println(separator);

            Scanner scanner = new Scanner(System.in);

            System.out.println("Digite a porta do servidor de nomes");
            int nameServerPort = Integer.parseInt(scanner.nextLine().trim());

            Registry registry = LocateRegistry.createRegistry(nameServerPort);
            registry.bind("nameServer", new NameServer());
            System.out.println("Servidor de nomes criado com sucesso!\n");
        }
        catch (Exception e)
        {
            System.out.println("Ocorreu um problema na criação do servidor de Nomes\n"+e.toString());
        }
    }
}
