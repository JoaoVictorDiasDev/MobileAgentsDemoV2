package main;

import main.Interfaces.IAgency;
import main.Interfaces.INameServer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class Agency extends UnicastRemoteObject implements Runnable, IAgency {
    private final LinkedList<Agent> agentsList = new LinkedList<>();
    public String agencyName;

    public int listeningPort;
    public String IP;

    private INameServer nameServer;
    private Registry agencyRegistry;

    public final Map<String, Thread> agentsThreads = new HashMap<>();

    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        new Agency();
    }

    public Agency() throws IOException, NotBoundException, AlreadyBoundException, InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        connectToNameServer();
        connectToAgencyServer();
        startAgency();
        startControlThread();
        getUserInput();
        stopAgency();
    }

    private void startControlThread() {
        Thread newThread = new Thread(this);
        newThread.start();
    }

    private void connectToAgencyServer() throws RemoteException {
        String message = "Conectando ao servidor de agências";
        String separator = "\u001B[1m" + "-".repeat(message.length());

        String reset = "\u001B[0m";  // Reset all formatting
        String bold = "\u001B[1m";   // Bold text
        String green = "\u001B[32m"; // Green text

        System.out.println(separator);
        System.out.println(bold + green + message + reset);
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o IP do servidor de agências");
        String agencyServerIP = scanner.nextLine().trim();

        System.out.println("Digite a porta do servidor de agências");
        int agencyServerPort = scanner.nextInt();

        agencyRegistry = LocateRegistry.getRegistry(agencyServerIP, agencyServerPort);
    }

    private void connectToNameServer() throws RemoteException, NotBoundException {
        String message = "Conectando ao servidor de nomes";
        String separator = "\u001B[1m" + "-".repeat(message.length());

        String reset = "\u001B[0m";  // Reset all formatting
        String bold = "\u001B[1m";   // Bold text
        String green = "\u001B[32m"; // Green text

        System.out.println(separator);
        System.out.println(bold + green + message + reset);
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o IP do servidor de nomes");
        String nameServerIP = scanner.nextLine().trim();

        System.out.println("Digite a porta do servidor de nomes");
        int nameServerPort = scanner.nextInt();

        Registry registryNameServer = LocateRegistry.getRegistry(nameServerIP, nameServerPort);
        nameServer = (INameServer) registryNameServer.lookup("nameServer");
    }

    @Override
    public void run (){
        var serverSocket = startServerSocket(listeningPort);

        Socket s;
        ObjectInputStream in;
        try {
            while (true) {
                System.out.printf("[%s] - Iniciou espera por novo agente\n", agencyName);
                s = serverSocket.accept();
                in = new ObjectInputStream(s.getInputStream());
                var receivedAgent = (Agent) in.readObject();
                receiveAgent(receivedAgent);
            }
        } catch (IOException e){
            System.out.println("Erro ao aceitar conexao na agency");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e){
            System.out.println("Erro ao ler agent na agency");
            throw new RuntimeException(e);
        }
    }

    public ServerSocket startServerSocket (int serverSocketPort){
        try {
            return new ServerSocket(serverSocketPort);
        } catch (IOException e) {
            System.out.println("Erro ao criar server socket na agency");
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String input) throws RemoteException, NotBoundException {
        var parts = input.split(" ");
        var message = input.split(" - ")[1];

        var agentName = parts[1];
        var agencyName = nameServer.getAgencyByAgent(agentName);
        var agency = (IAgency) agencyRegistry.lookup(agencyName);
        agency.receiveMessage(message, agentName);
    }

    public void receiveMessage (String msg, String receiverAgentName) throws RemoteException, NotBoundException {
        var agent = findAgentByName(receiverAgentName);
        agent.receiveMessage(msg);
    }

    public void receiveAgent (Agent agent){
        agent.currentAgencyName = this.agencyName;
        agentsList.addFirst(agent);
        Thread newAgentThread = new Thread(agent);
        newAgentThread.start();
        agentsThreads.put(agent.agentName, newAgentThread);

        System.out.printf("[%s] - Rodando agente: %s\n", this.agencyName, agent.agentName);
        agentsList.add(agent);
    }

    @Override
    public String getAgencyName() throws RemoteException {
        return this.agencyName;
    }

    @Override
    public String getAgencyHost() throws RemoteException {
        return this.IP;
    }

    @Override
    public int getAgencyPort() throws RemoteException {
        return this.listeningPort;
    }

    @Override
    public Map<String, Thread> getAgentsThreads() throws RemoteException {
       return agentsThreads;
    }

    @Override
    public boolean equals(Object o){
        if (o == null || getClass() != o.getClass()) return false;
        var agency = (Agency) o;
        return Objects.equals(agency.agencyName, this.agencyName);
    }

    public Agent findAgentByName(String targetAgentName) {
        Optional<Agent> result = agentsList.stream()
                .filter(agent -> agent.agentName.equals(targetAgentName))
                .findFirst();
        return result.orElse(null); // Return found Agent or null
    }

    public void startAgency() throws AlreadyBoundException, RemoteException {
        // Imprime cabeçalho
        String message = "Procedimento de criação de agência";
        String separator = "\u001B[1m" + "-".repeat(message.length());

        String reset = "\u001B[0m";  // Reset all formatting
        String bold = "\u001B[1m";   // Bold text
        String green = "\u001B[32m"; // Green text

        System.out.println(separator);
        System.out.println(bold + green + message + reset);
        System.out.println(separator);


        // Pega dados da agência
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o IP da agência");
        IP = scanner.nextLine().trim();

        System.out.println("Digite a porta da agência");
        listeningPort = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Digite o nome da agência");
        agencyName = scanner.nextLine().trim();

        // Registra agência como objeto remoto
        agencyRegistry.bind(agencyName, this);
    }

    public void getUserInput() throws NotBoundException, IOException, InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var exitRequested = false;
        var scanner = new Scanner(System.in);
        while (!exitRequested) {
            try {
                System.out.println("Digite um comando (create-agent agentName | send-message agentName msg | stop-agent agentName | move-agent agentName agencyName | status | exit):");
                String input = scanner.nextLine().trim();

                if(input.contains("create-agent") && verifyInput(input)) {
                    createAgent(input);
                }

                if(input.equals("status") && verifyInput(input)) {
                    status();
                }

                if(input.equals("thread-status") && verifyInput(input)) {
                    threadStatus();
                }

                if(input.contains("send-message")) {
                    sendMessage(input);
                }

                if(input.contains("move-agent") && verifyInput(input)) {
                    moveAgent(input);
                }

                if(input.contains("stop-agent") && verifyInput(input)) {
                    stopAgent(input);
                }

                if(input.equals("stop-agency") && verifyInput(input)) {
                    exitRequested = true;
                }
            } catch (Exception e){
                System.out.println("------- ERRO -------\n" + e.getMessage());
            }
        }
        scanner.close();
    }

    private void stopAgency() throws RemoteException {
        System.out.printf("[%s] - Iniciando processo de desligamento da agência\n", getAgencyName());
        var map = new HashMap<>(agentsThreads);
        map.forEach((key, value) -> {
            value.interrupt();
            agentsThreads.remove(key);
            try {
                nameServer.removeAgent(key);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            System.out.printf("[%s] - Interrompendo %s e removendo do servidor de nomes\n", agencyName, key);
        });
    }

    private void stopAgent(String input) throws RemoteException {
        var parts = input.split(" ");
        var agentName = parts[1];
        var agent = findAgentByName(agentName);

        //Para Thread do agente sendo transferido
        agentsThreads.get(agentName).interrupt();
        agentsThreads.remove(agentName);

        // Remove agente da lista de agentes em execução
        agentsList.remove(agent);

        // Atualiza servidor de nomes para refletir nova agência do agente
        nameServer.removeAgent(agentName);
    }

    public void createAgent(String input) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Pega dados do agente
        var parts = input.split(" ");
        var agentName = parts[1];
        var pathToAgentCode = parts[2];

        // Carrega código a partir do caminho fornecido
        File agentFile = new File(pathToAgentCode);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{agentFile.toURI().toURL()});
        Class<?> agentClass = classLoader.loadClass("main.Agent");
        Constructor<?> agentClassConstructor = agentClass.getConstructors()[0];
        Agent agent = (Agent) agentClassConstructor.newInstance(agentName, getAgencyName(), nameServer, agencyRegistry);

        Thread newAgentThread = new Thread(agent);
        newAgentThread.start();

        agentsThreads.put(agentName, newAgentThread);
        agentsList.add(agent);


        nameServer.associateAgentWithAgency(agentName, agencyName);
        System.out.printf("[%s] - Rodando agente: %s\n", this.agencyName, agent.agentName);
    }

    public void moveAgent(String input) throws NotBoundException, RemoteException {
        // Pega dados do agente e agências envolvidos na transação
        var parts = input.split(" ");
        var agentName = parts[1];
        var destAgencyName = parts[2];
        var currentAgencyName = nameServer.getAgencyByAgent(agentName);
        var currentAgency = (IAgency) agencyRegistry.lookup(currentAgencyName);

        // Chama método para transferência na agência que contém o agente transferido
        currentAgency.sendAgent(agentName, destAgencyName);
    }

    public void sendAgent (String agentName, String destAgencyName) throws NotBoundException, RemoteException {
        var agent = findAgentByName(agentName);
        var destAgency = (IAgency) agencyRegistry.lookup(destAgencyName);

        //Para Thread do agente sendo transferido
        getAgentsThreads().get(agentName).interrupt();
        getAgentsThreads().remove(agentName);

        // Remove agente da lista de agentes em execução
        agentsList.remove(agent);

        // Atualiza servidor de nomes para refletir nova agência do agente
        nameServer.associateAgentWithAgency(agentName, destAgencyName);

        // Conecta com agência de destino e envia agente serializado pela redes
        try {
            Socket s = new Socket(destAgency.getAgencyHost(), destAgency.getAgencyPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(agent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void status() throws RemoteException {
        var map = nameServer.getMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Agent: " + key + ", Agency: " + value);
        }
    }

    private boolean verifyInput(String input) {
        if(input.contains("send-message")) {
            var parts = input.split(" ");
            if(parts.length != 3) {
                System.out.println("Formatação incorreta para comando \"send-message\". Tente novamente");
                return false;
            }
        }
        return true;
    }

    private void threadStatus() {
        for (Map.Entry<String, Thread> entry : agentsThreads.entrySet()) {
            String key = entry.getKey();
            Thread thread = entry.getValue();
            System.out.println("Agent: " + key + ", Thread: " + thread);
        }
    }
}
