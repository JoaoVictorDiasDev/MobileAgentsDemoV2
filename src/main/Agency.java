package main;

import java.io.IOException;
import java.io.ObjectInputStream;
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
    // List de agentes que estão atualmente associados com essa agência
    private final LinkedList<Agent> agentsList;
    public final String agencyName;

    public final int listeningPort;
    public final String host;
    private final INameServer nameServer;

    private Registry registryNameServer;
    private Registry registryAgencyServer;

    public Agency(String agencyName, String host, int listeningPort) throws RemoteException, NotBoundException, AlreadyBoundException {
        super();

        agentsList = new LinkedList<>();

        this.agencyName = agencyName;
        this.listeningPort = listeningPort;
        this.host = host;

        registryNameServer = LocateRegistry.getRegistry(nameServerHost, nameServerPort);
        nameServer = (INameServer) registryNameServer.lookup("nameServer");
    }

    public void addAgent(Agent agent){
        agent.currentAgency = this;
        agentsList.addFirst(agent);
        runAgent(agent);
        System.out.println("Running Agent");
    }

    public void runAgent(Agent targetAgent) {
        var agent = findAgent(targetAgent);

        if(agent != null) {
            Thread newAgentThread = new Thread(agent);
            newAgentThread.start();
        } else throw new RuntimeException();
        // TODO: Potencialmente criar excecao personalizada aqui
    }

    @Override
    public void run (){
        System.out.println("Running Agency!");
        var serverSocket = startServerSocket(listeningPort);

        Socket s;
        ObjectInputStream in;
        try {
            while (true) {
                System.out.println("Agency " + agencyName + " started loop");
                s = serverSocket.accept();

                in = new ObjectInputStream(s.getInputStream());
                addAgent((Agent) in.readObject());
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

    public void sendMessage(String msg, Agent receiverAgent) throws NotBoundException, RemoteException {
        var agency = getAgencyByAgent(receiverAgent);
        agency.receiveMessage(msg, receiverAgent);
    }

    public void receiveMessage (String msg, Agent receiverAgent) {
        var agent = findAgent(receiverAgent);
        agent.receiveMessage(msg);
    }

    public Agent findAgent(Agent targetAgent) {
        Optional<Agent> result = agentsList.stream()
                .filter(agent -> agent.equals(targetAgent))
                .findFirst();
        return result.orElse(null); // Return found Agent or null
    }

    @Override
    public boolean equals(Object o){
        if (o == null || getClass() != o.getClass()) return false;
        var agency = (Agency) o;
        return Objects.equals(agency.agencyName, this.agencyName);
    }

    public Agency getAgencyByAgent(Agent agent) throws NotBoundException, RemoteException {
        var agency = nameServer.getAgencyByAgent(agent);
        return (Agency) registryAgencyServer.lookup(agency.agencyName);
    }
}
