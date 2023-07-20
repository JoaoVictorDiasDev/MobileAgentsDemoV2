package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Agency implements Runnable{
    // List de agentes que estão atualmente associados com essa agência
    private final LinkedList<Agent> agentsList;
    public final String agencyName;

    public final int listeningPort;
    public final String host;

    public Agency(String agencyName, String host, int listeningPort) {
        agentsList = new LinkedList<>();

        this.agencyName = agencyName;
        this.listeningPort = listeningPort;
        this.host = host;
    }

    public void addAgent(Agent agent){
        agent.currentAgency = this;
        agentsList.addFirst(agent);
        runAgent(agent);
        System.out.println("Running Agent");
    }

    public void runAgent(Agent agent) {
        for(Agent a : agentsList) {
            if(a.agentName.equals(agent.agentName)) {
                Thread newAgentThread = new Thread(a);
                newAgentThread.start();
            }
        }
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
}
