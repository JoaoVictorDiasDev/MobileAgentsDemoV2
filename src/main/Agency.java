package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class Agency extends UnicastRemoteObject implements Runnable, IAgency{
    private final LinkedList<Agent> agentsList = new LinkedList<>();
    public String agencyName;

    public int listeningPort;
    public String IP;

    private INameServer nameServer;
    private Registry agencyRegistry;

    private final Map<String, Thread> agentsThreads = new HashMap<>();

    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, RemoteException, InterruptedException {
        new Agency();
    }

    public Agency() throws RemoteException, NotBoundException, AlreadyBoundException, InterruptedException {
        connectToNameServer();
        connectToAgencyServer();
        startAgency();
        startControlThread();
        getUserInput();
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
        System.out.printf("[%s] - Thread iniciada!\n", agencyName);
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

        var agentName = parts[1];
        var msg = parts[2];
        var agencyName = nameServer.getAgencyByAgent(agentName);
        var agency = (IAgency) agencyRegistry.lookup(agencyName);
        agency.receiveMessage(msg, agentName);
    }

    public void receiveMessage (String msg, String receiverAgentName) throws  RemoteException{
        var agent = findAgentByName(receiverAgentName);
        agent.receiveMessage(msg);
    }

    public void sendAgent (String destinationAgencyName, Agent agent) throws NotBoundException, RemoteException {
        var agency = (IAgency) agencyRegistry.lookup(destinationAgencyName);
        try {
            Socket s = new Socket(agency.getAgencyHost(), agency.getAgencyPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(agent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public IAgency getAgencyByAgent(String agentName) throws NotBoundException, RemoteException {
        var agencyName = nameServer.getAgencyByAgent(agentName);
        return (IAgency) agencyRegistry.lookup(agencyName);
    }

    public void startAgency() throws AlreadyBoundException, RemoteException {
        String message = "Procedimento de criação de agência";
        String separator = "\u001B[1m" + "-".repeat(message.length());

        String reset = "\u001B[0m";  // Reset all formatting
        String bold = "\u001B[1m";   // Bold text
        String green = "\u001B[32m"; // Green text

        System.out.println(separator);
        System.out.println(bold + green + message + reset);
        System.out.println(separator);


        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o IP da agência");
        IP = scanner.nextLine().trim();

        System.out.println("Digite a porta da agência");
        listeningPort = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Digite o nome da agência");
        agencyName = scanner.nextLine().trim();

        agencyRegistry.bind(agencyName, this);
    }

    public void getUserInput() throws NotBoundException, RemoteException, InterruptedException {
        var exitRequested = false;
        var scanner = new Scanner(System.in);
        while (!exitRequested) {
            System.out.println("Digite um comando (create-agent | send-message | stop-agent):");
            String input = scanner.nextLine().trim();

            if(input.contains("create-agent") && verifyInput(input)) {
                createAgent(input);
            }

            if(input.contains("status") && verifyInput(input)) {
                status();
            }

            if(input.contains("send-message") && verifyInput(input)) {
                sendMessage(input);
            }

            if(input.contains("exit")){
                exitRequested = true;
            }

            if(input.contains("move-agent") && verifyInput(input)) {
                moveAgent(input);
            }

//            switch (input) {
//                case "status":
//                    break;
//                case input "send-message":
//                    executarComando2();
//                    break;
//                case "stop-agent":
//                    executarComando2();
//                    break;
//                case "sair":
//                    exitRequested = true;
//                    break;
//                default:
//                    System.out.println("Comando inválido. Tente novamente.");
//            }
        }
        scanner.close();
    }

    public void createAgent(String input) throws RemoteException {
        var parts = input.split(" ");
        var agentName = parts[1];

        Agent agent = new Agent(agentName);
        agent.currentAgencyName = this.agencyName;
        Thread newAgentThread = new Thread(agent);
        newAgentThread.start();
        agentsList.add(agent);
        nameServer.associateAgentWithAgency(agentName, agencyName);
        System.out.printf("[%s] - Rodando agente: %s\n", this.agencyName, agent.agentName);
    }

    public void moveAgent(String input) throws NotBoundException, RemoteException, InterruptedException {
        // Pega dados do agente e agência envolvidos na transação
        var parts = input.split(" ");
        var agentName = parts[1];
        var agencyName = parts[2];
        var agent = findAgentByName(agentName);
        var agency = (IAgency) agencyRegistry.lookup(agencyName);

        //Para Thread do agente sendo transferido
        agentsThreads.get(agentName).interrupt();
        agentsThreads.remove(agentName);

        // Remove agente da lista de agentes em execução
        agentsList.remove(agent);

        // Atualiza servidor de nomes para refletir nova agência do agente
        nameServer.associateAgentWithAgency(agentName, agency.getAgencyName());

        // Conecta com agência de destino e envia agente serializado pela redes
        try {
            Socket s = new Socket(agency.getAgencyHost(), agency.getAgencyPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(findAgentByName(agentName));
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
}
