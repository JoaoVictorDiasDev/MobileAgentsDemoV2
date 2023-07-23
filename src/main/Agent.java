package main;

import main.Interfaces.IAgency;
import main.Interfaces.INameServer;
import main.Servers.NameServer;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;


public class Agent implements Runnable, Serializable {

    protected final String agentName;
    public String currentAgencyName;
    private final INameServer nameServer;
    private final Registry agencyRegistry;

    public Agent(String name, String currentAgencyName, INameServer nameServer, Registry agencyRegistry) throws RemoteException {
        this.agentName = name;
        this.currentAgencyName = currentAgencyName;
        this.nameServer = nameServer;
        this.agencyRegistry = agencyRegistry;
    }

    public void onArrival(){
        System.out.printf("[%s] - chegou na agência %s\n", agentName, currentAgencyName);
        try { Thread.sleep(5000); } catch (InterruptedException ie) {};
        System.out.printf("[%s] - finalizou dormir\n", agentName);
    }

//    public void sendMessage(String msg, Agent recieverAgent) throws NotBoundException, RemoteException {
//        var agency = (IAgency) registryAgencyServer.lookup(currentAgencyName);
//        agency.sendMessage(msg, recieverAgent);
//    }

    public void receiveMessage(String msg) throws RemoteException, NotBoundException {
        System.out.printf("[%s] - Recebi mensagem: %s\n", agentName, msg);
        if(msg.contains("migrate-to")) {
            var parts = msg.split(" ");
            var destAgencyName = parts[1];

            var currentAgencyName = nameServer.getMap().get(agentName);
            var currentAgency = (IAgency) agencyRegistry.lookup(currentAgencyName);
            currentAgency.sendAgent(agentName, destAgencyName);

        }
    }

    @Override
    public void run() {
        onArrival();
    }

    @Override
    public boolean equals(Object o){
        if (o == null || getClass() != o.getClass()) return false;
        var a1 = (Agent) o;
        return Objects.equals(a1.agentName, this.agentName);
    }
}
