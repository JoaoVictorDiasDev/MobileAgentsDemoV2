package main;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

public abstract class Agent implements Runnable, Serializable {

    protected final String agentName;
    public Agency currentAgency;

    public Agent(String name, Agency currentAgency) {
        this.agentName = name;
        this.currentAgency = currentAgency;
    }

    public abstract void onArrival();

    public void sendMessage(String msg, Agent recieverAgent) throws NotBoundException, RemoteException {
       currentAgency.sendMessage(msg, recieverAgent);
    }

    public void receiveMessage(String msg) {
        //TODO: Comportamento ao receber mensagem
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
