package main;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Objects;


public class Agent implements Runnable, Serializable {

    protected final String agentName;
    public String currentAgencyName;
    private final INameServer nameServer;
    private final Registry agencyRegistry;

    public Agent(String name, String currentAgencyName, INameServer nameServer, Registry agencyRegistry) {
        this.agentName = name;
        this.currentAgencyName = currentAgencyName;
        this.nameServer = nameServer;
        this.agencyRegistry = agencyRegistry;
    }

    // Método que contém funções executadas ao chegar em uma nova agência
    public void onArrival(){
        System.out.printf("[%s] - chegou na agência %s\n", agentName, currentAgencyName);
        System.out.printf("[%s] - Pronto para executar qualquer atividade!\n", agentName);
    }

    // Método para tratamento de mensagens
    public void receiveMessage(String msg) throws RemoteException, NotBoundException {
        System.out.printf("[%s] - Recebi mensagem: %s\n", agentName, msg);

        // Pega dados para tratamento de mensagem
        var currentAgencyName = nameServer.getMap().get(agentName);
        var currentAgency = (IAgency) agencyRegistry.lookup(currentAgencyName);

       // Trata mensagem caso haja migrate-to
        if(msg.contains("migrate-to")) {
            var parts = msg.split(" ");
            var destAgencyName = parts[1];

            System.out.printf("[%s] - Sendo transferido para agência: %s\n", agentName, destAgencyName);
            currentAgency.sendAgent(agentName, destAgencyName);
        }

        // Trata mensagem caso haja start-reunion
        if(msg.contains("start-reunion")) {
            var parts = msg.split(" ");
            for(int i = 1; i < parts.length; i++) {
                var msgToSend = String.format("send-message %s - migrate-to %s", parts[i], currentAgencyName);
                System.out.printf("[%s] - Enviando mensagem: %s\n", agentName, msgToSend);
                currentAgency.sendMessage(msgToSend);
            }
        }
    }

    // Chama onArrival toda vez que uma nova thread é iniciada
    @Override
    public void run() {
        onArrival();
    }

    // Override em equals para comparar por nome
    @Override
    public boolean equals(Object o){
        if (o == null || getClass() != o.getClass()) return false;
        var a1 = (Agent) o;
        return Objects.equals(a1.agentName, this.agentName);
    }
}
