package main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Objects;

import static main.Util.agencyServerPort;
import static main.Util.nameServerPort;

@SuppressWarnings("resource")
public class Client {

    private static final LinkedList<Agency> agencyList = new LinkedList<>();
    private static final LinkedList<Agent> agentList = new LinkedList<>();
    private static final INameServer nameServer = createNameServer();
    private static final Registry agencyRegistry = createAgencyRegistry();

    public static void main(String[] args) throws NotBoundException, RemoteException, AlreadyBoundException {

        createAgency("S1", "localhost", 9000);
        createAgency("S2", "localhost", 8000);
        createAgency("S3", "localhost", 7000);

        createAgent("A1");
        createAgent("A2");
        createAgent("A3");
        createAgent("A4");


        moveAgent("A1", "S1");
        moveAgent("A2", "S2");
        moveAgent("A3", "S3");
        moveAgent("A4", "S1");

        agentList.getFirst().sendMessage("Olar", agentList.get(2));

    }

    public static void createAgency(String agencyName, String host, int listeningPort) throws NotBoundException, RemoteException, AlreadyBoundException {
        Agency agency = new Agency(agencyName, host, listeningPort);
        agencyList.add(agency);

        agencyRegistry.bind(agencyName, agency);
        System.out.println("Adicionou agência " + agencyName + " ao registro de agências com sucesso");

        Thread newThread = new Thread(agency);
        newThread.start();
    }

    public static void createAgent(String agentName){
        Agent agent = new Kilroy(agentName);
        agentList.add(agent);
    }

    public static void moveAgent(String agentName, String agencyName) throws RemoteException {
        Agent agent = null;
        Agency agency = null;
        for(Agent a : agentList)
            if (Objects.equals(a.agentName, agentName)) agent = a;

        for(Agency s : agencyList)
            if (Objects.equals(s.agencyName, agencyName)) agency = s;

        try {
            Socket s = new Socket(agency.host, agency.listeningPort);
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(agent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        nameServer.associateAgentWithAgency(agent, agency);
    }

    private static INameServer createNameServer() {
        try
        {
            System.out.println("Criando servidor de nomes...");
            Registry registry = LocateRegistry.createRegistry(nameServerPort);
            registry.bind("nameServer", new NameServer());
            System.out.println("Servidor de nomes criado com sucesso...");
            return (INameServer) registry.lookup("nameServer");
        }
        catch (Exception e)
        {
            System.out.println("Ocorreu um problema na criação do servidor de Nomes\n"+e.toString());
        }
        throw new RuntimeException();
    }

    private static Registry createAgencyRegistry() {
        try
        {
            System.out.println("Criando registro de agencias...");
            return LocateRegistry.createRegistry(agencyServerPort);
        }
        catch (Exception e)
        {
            System.out.println("Ocorreu um problema na criação do servidor de Nomes\n"+e.toString());
        }
        throw new RuntimeException();
    }
}
