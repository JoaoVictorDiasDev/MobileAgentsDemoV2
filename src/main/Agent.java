package main;

import java.io.*;
import java.net.Socket;

public abstract class Agent implements Runnable, Serializable {

    protected final String agentName;
    public Agency currentAgency;

    public Agent(String name) {
        this.agentName = name;

    }

    public abstract void onArrival();

    @Override
    public void run() {
        onArrival();
    }
}
