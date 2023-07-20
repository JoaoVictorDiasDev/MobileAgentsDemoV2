package main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

@SuppressWarnings("resource")
public class AgencyServer {

    private static final LinkedList<Agency> agencyList = new LinkedList<>();
    private static final LinkedList<Agent> agentList = new LinkedList<>();

    public static void main(String[] args) {

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

    }

    public static void createAgency(String agencyName, String host, int listeningPort){
        Agency agency = new Agency(agencyName, host, listeningPort);
        agencyList.add(agency);
        Thread newThread = new Thread(agency);
        newThread.start();
    }

    public static void createAgent(String agentName){
        Agent agent = new Kilroy(agentName);
        agentList.add(agent);
    }

    public static void moveAgent(String agentName, String agencyName){
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

        }
    }
}
