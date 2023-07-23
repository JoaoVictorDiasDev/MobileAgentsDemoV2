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
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static main.Util.agencyServerPort;
import static main.Util.nameServerPort;

@SuppressWarnings("resource")
public class Client {

    private static final LinkedList<Agency> agencyList = new LinkedList<>();
    private static final LinkedList<Agent> agentList = new LinkedList<>();
    private static final INameServer nameServer = null;
    private static final Registry agencyRegistry = createAgencyRegistry();

    public static void main(String[] args) throws NotBoundException, RemoteException, AlreadyBoundException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        boolean exitRequested = false;

        System.out.println("-----------------------------------------");
        createAgency("S1", "localhost", 9000);
        createAgency("S2", "localhost", 8000);
        createAgency("S3", "localhost", 7000);
        System.out.println("-----------------------------------------");

        createAgent("A1");
        createAgent("A2");
        createAgent("A3");
        createAgent("A4");
        System.out.println("-----------------------------------------");


        moveAgent("A1", "S1");
        moveAgent("A2", "S2");
        moveAgent("A3", "S3");
        moveAgent("A4", "S1");
        moveAgent("A2", "S1");
        System.out.println("-----------------------------------------");

    }



    public static void createAgency(String agencyName, String host, int listeningPort) throws NotBoundException, RemoteException, AlreadyBoundException, InterruptedException {
        Agency agency = new Agency();
        agencyList.add(agency);

        agencyRegistry.bind(agencyName, agency);
        System.out.println("Adicionou agência " + agencyName + " ao registro de agências com sucesso");

        Thread newThread = new Thread(agency);
        newThread.start();
    }

    public static void createAgent(String agentName) throws RemoteException {
        Agent agent = new Agent(agentName);
        agentList.add(agent);
    }

    public static void moveAgent(String agentName, String agencyName) throws RemoteException, NotBoundException {
        Agent agent = null;
        for(Agent a : agentList)
            if (Objects.equals(a.agentName, agentName)) agent = a;

        var agency = (IAgency) agencyRegistry.lookup(agencyName);

        try {
            Socket s = new Socket(agency.getAgencyHost(), agency.getAgencyPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(agent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        agent.currentAgencyName = agency.getAgencyName();
        nameServer.associateAgentWithAgency(agent.agentName, agency.getAgencyName());
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
