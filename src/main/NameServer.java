package main;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class NameServer extends UnicastRemoteObject implements INameServer {
    private Map<Agent, Agency> map = new HashMap<>();

    protected NameServer() throws RemoteException {
        super();
    }

    @Override
    public Agency getAgencyByAgent(Agent agent) {
        return map.get(agent);
    }

    @Override
    public void associateAgentWithAgency(Agent agent, Agency agency) throws RemoteException {
        map.put(agent, agency);
    }
}
