package main;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IAgency extends Remote {
    void sendMessage(String input) throws RemoteException, NotBoundException;
    void receiveMessage (String msg, String receiverAgentName) throws RemoteException, NotBoundException;
    String getAgencyName() throws RemoteException;
    String getAgencyHost() throws RemoteException;
    int getAgencyPort() throws RemoteException;
    Map<String, Thread> getAgentsThreads() throws RemoteException;
    void sendAgent (String agentName, String agencyName) throws NotBoundException, RemoteException;
    void moveAgent(String input) throws NotBoundException, RemoteException;
}

