package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import static main.Util.*;

public class Agency extends UnicastRemoteObject implements Runnable, IAgency{
    private final LinkedList<Agent> agentsList;
    public final String agencyName;

    public final int listeningPort;
    public final String host;
    private final INameServer nameServer;

    private final Registry agencyRegistry;

    public Agency(String agencyName, String host, int listeningPort) throws RemoteException, NotBoundException, AlreadyBoundException {
        super();

        agentsList = new LinkedList<>();

        this.agencyName = agencyName;
        this.listeningPort = listeningPort;
        this.host = host;

        Registry registryNameServer = LocateRegistry.getRegistry(nameServerHost, nameServerPort);
        nameServer = (INameServer) registryNameServer.lookup("nameServer");
        agencyRegistry = LocateRegistry.getRegistry(agencyServerHost, agencyServerPort);
    }

    public void addAgent(Agent agent){
    }

    @Override
    public void run (){
        System.out.printf("[%s] - Thread iniciada!\n", agencyName);
        var serverSocket = startServerSocket(listeningPort);

        Socket s;
        ObjectInputStream in;
        try {
            while (true) {
                System.out.printf("[%s] - Iniciou espera por novo agente\n", agencyName);
                s = serverSocket.accept();
                in = new ObjectInputStream(s.getInputStream());
                var receivedAgent = (Agent) in.readObject();
                receiveAgent(receivedAgent);
            }
        } catch (IOException e){
            System.out.println("Erro ao aceitar conexao na agency");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e){
            System.out.println("Erro ao ler agent na agency");
            throw new RuntimeException(e);
        }
    }

    public ServerSocket startServerSocket (int serverSocketPort){
        try {
            return new ServerSocket(serverSocketPort);
        } catch (IOException e) {
            System.out.println("Erro ao criar server socket na agency");
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg, String receiverAgentName) throws NotBoundException, RemoteException {
        var agency = getAgencyByAgent(receiverAgentName);
        agency.receiveMessage(msg, receiverAgentName);
    }

    public void receiveMessage (String msg, String receiverAgentName) throws  RemoteException{
        var agent = findAgentByName(receiverAgentName);
        agent.receiveMessage(msg);
    }

    public void sendAgent (String destinationAgencyName, Agent agent) throws NotBoundException, RemoteException {
        var agency = (IAgency) agencyRegistry.lookup(destinationAgencyName);
        try {
            Socket s = new Socket(agency.getAgencyHost(), agency.getAgencyPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(agent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveAgent (Agent agent){
        agent.currentAgencyName = this.agencyName;
        agentsList.addFirst(agent);
        Thread newAgentThread = new Thread(agent);
        newAgentThread.start();
        System.out.printf("[%s] - Rodando agente: %s\n", this.agencyName, agent.agentName);
        addAgent(agent);
    }

    @Override
    public String getAgencyName() throws RemoteException {
        return this.agencyName;
    }

    @Override
    public String getAgencyHost() throws RemoteException {
        return this.host;
    }

    @Override
    public int getAgencyPort() throws RemoteException {
        return this.listeningPort;
    }


    @Override
    public boolean equals(Object o){
        if (o == null || getClass() != o.getClass()) return false;
        var agency = (Agency) o;
        return Objects.equals(agency.agencyName, this.agencyName);
    }

    public Agent findAgentByName(String targetAgentName) {
        Optional<Agent> result = agentsList.stream()
                .filter(agent -> agent.agentName.equals(targetAgentName))
                .findFirst();
        return result.orElse(null); // Return found Agent or null
    }

    public IAgency getAgencyByAgent(String agentName) throws NotBoundException, RemoteException {
        var agencyName = nameServer.getAgencyByAgent(agentName);
        return (IAgency) agencyRegistry.lookup(agencyName);
    }
}
