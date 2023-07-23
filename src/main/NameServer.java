package main;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class NameServer extends UnicastRemoteObject implements INameServer {
    private final Map<String, String> map = new HashMap<>();

    protected NameServer() throws RemoteException {
        super();
    }

    @Override
    public String getAgencyByAgent(String agentName) {
        return map.get(agentName);
    }

    @Override
    public void associateAgentWithAgency(String agentName, String agencyName) throws RemoteException {
        map.put(agentName, agencyName);
    }

    @Override
    public Map<String, String> getMap() throws RemoteException {
        return map;
    }
}
