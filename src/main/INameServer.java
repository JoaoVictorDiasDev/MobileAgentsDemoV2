package main;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INameServer extends Remote {
    public Agency getAgencyByAgent(Agent agent) throws RemoteException;
    public void associateAgentWithAgency(Agent agent, Agency agency) throws RemoteException;
}
