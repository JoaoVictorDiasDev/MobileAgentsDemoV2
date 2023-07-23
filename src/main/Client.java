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
    private static final INameServer nameServer = createNameServer();
    private static final Registry agencyRegistry = createAgencyRegistry();

    public static void main(String[] args) throws NotBoundException, RemoteException, AlreadyBoundException {
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

        while (!exitRequested) {
            System.out.println("Digite um comando (create-agent | send-message | stop-agent):");
            String input = scanner.nextLine().trim();

            if(input.contains("create-agent") && verifyInput(input)) {
                createAgent(input);
            }

            if(input.contains("status") && verifyInput(input)) {
                status(input);
            }

            if(input.contains("send-message") && verifyInput(input)) {
                sendMessage(input);
            }

            if(input.contains("exit")){
                exitRequested = true;
            }

//            switch (input) {
//                case "status":
//                    break;
//                case input "send-message":
//                    executarComando2();
//                    break;
//                case "stop-agent":
//                    executarComando2();
//                    break;
//                case "sair":
//                    exitRequested = true;
//                    break;
//                default:
//                    System.out.println("Comando inválido. Tente novamente.");
//            }
        }

        scanner.close();



    }

    private static void status(String input) throws RemoteException {
        var map = nameServer.getMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }

    private static boolean verifyInput(String input) {
        if(input.contains("send-message")) {
            var parts = input.split(" ");
            if(parts.length != 3) {
                System.out.println("Formatação incorreta para comando \"send-message\". Tente novamente");
                return false;
            }
        }

        return true;
    }

    public static void createAgency(String agencyName, String host, int listeningPort) throws NotBoundException, RemoteException, AlreadyBoundException {
        Agency agency = new Agency(agencyName, host, listeningPort);
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

    private static void sendMessage(String input) throws RemoteException, NotBoundException {
        var parts = input.split(" ");

        var agentName = parts[1];
        var msg = parts[2];
        var agencyName = nameServer.getAgencyByAgent(agentName);
        var agency = (IAgency) agencyRegistry.lookup(agencyName);
        agency.sendMessage(msg, agentName);
    }
}
