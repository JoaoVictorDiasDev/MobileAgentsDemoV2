package main;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface INameServer extends Remote {
    String getAgencyByAgent(String agentName) throws RemoteException;
    void associateAgentWithAgency(String agentName, String agencyName) throws RemoteException;
    void removeAgent (String agentName) throws RemoteException;

    Map<String, String> getMap() throws RemoteException;
}
