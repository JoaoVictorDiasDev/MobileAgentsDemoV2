package main;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAgency extends Remote {

    void sendMessage(String input) throws RemoteException, NotBoundException;
    void receiveMessage (String msg, String receiverAgentName) throws RemoteException;
    String getAgencyName() throws RemoteException;
    String getAgencyHost() throws RemoteException;
    int getAgencyPort() throws RemoteException;
    void sendAgent (String destinationAgencyName, Agent agent) throws NotBoundException, RemoteException;

}

