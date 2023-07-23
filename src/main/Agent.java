package main;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;

import static main.Util.*;

public class Agent implements Runnable, Serializable {

    protected final String agentName;
    public String currentAgencyName;
    public Registry registryAgencyServer;

    public Agent(String name) throws RemoteException {
        this.agentName = name;
        registryAgencyServer = LocateRegistry.getRegistry(agencyServerHost, agencyServerPort);
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

    public void receiveMessage(String msg) {
        System.out.printf("[%s] - Recebi mensagem: %s\n", agentName, msg);
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
