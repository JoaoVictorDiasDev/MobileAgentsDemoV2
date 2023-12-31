package main;

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

    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException {
        new Agency();
    }

    public Agency() throws IOException, NotBoundException, AlreadyBoundException {
        connectToNameServer();
        connectToAgencyServer();
        startAgency();
        startControlThread();
        getUserInput();
        stopAgency();
    }

    // Inicia thread de controle (espera por chegada de novo agente)
    private void startControlThread() {
        Thread newThread = new Thread(this);
        newThread.start();
    }

    // Conecta com servidor de agências
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

    // Conecta com servidor de nomes
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
        // Cria serverSocket na porta fornecida pelo usuário
        var serverSocket = startServerSocket(listeningPort);
        Socket s;
        ObjectInputStream in;
        try {
            while (true) {
                System.out.printf("[%s] - Iniciou espera por novo agente\n", agencyName);
                // Aceita todas as tentativas de conexão na porta
                s = serverSocket.accept();
                // Lê objeto enviado pela máquina conectada
                in = new ObjectInputStream(s.getInputStream());
                // Converte objeto para Agente
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

    // Inicia ServerSocket
    public ServerSocket startServerSocket (int serverSocketPort){
        try {
            return new ServerSocket(serverSocketPort);
        } catch (IOException e) {
            System.out.println("Erro ao criar server socket na agency");
            throw new RuntimeException(e);
        }
    }

    // Método para envio de mensagem
    public void sendMessage(String input) throws RemoteException, NotBoundException {
        // Trata input para separar mensagem e nome do agente
        var parts = input.split(" ");
        var message = input.split(" - ")[1];

        // Busca agência pelo servidor de nomes e agências
        var agentName = parts[1];
        var agencyName = nameServer.getAgencyByAgent(agentName);
        var agency = (IAgency) agencyRegistry.lookup(agencyName);

        // Chama método na agência que contém o agente para recebimento de mensagem
        agency.receiveMessage(message, agentName);
    }

    // Método que recebe mensagem e encaminha para agente de destino
    public void receiveMessage (String msg, String receiverAgentName) throws RemoteException, NotBoundException {
        var agent = findAgentByName(receiverAgentName);
        agent.receiveMessage(msg);
    }

    // Método para recebimento de novos agentes
    public void receiveAgent (Agent agent){
        // Configura novo agente
        agent.currentAgencyName = this.agencyName;
        agentsList.addFirst(agent);

        // Cria e executa nova thread para agente
        Thread newAgentThread = new Thread(agent);
        newAgentThread.start();

        // Salva mapeamento agente-thread
        agentsThreads.put(agent.agentName, newAgentThread);
        System.out.printf("[%s] - Rodando agente: %s\n", this.agencyName, agent.agentName);
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

    // Método utilitário para encontrar um agente na lista através de seu nome
    public Agent findAgentByName(String targetAgentName) {
        Optional<Agent> result = agentsList.stream()
                .filter(agent -> agent.agentName.equals(targetAgentName))
                .findFirst();
        return result.orElse(null);
    }

    // Método para configuração inicial da agência
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

    // Loop de leitura de input do usuário
    public void getUserInput() {
        var exitRequested = false;
        var scanner = new Scanner(System.in);
        while (!exitRequested) {
            try {
                String message = """
                        Listagem Comandos:
                        create-agent [agentName]
                        send-message [agentName] [msg]
                        move-agent [agentName] [agencyName]
                        stop-agent [agentName]
                        stop-agency
                        status
                        thread-status""";
                String separator = "\u001B[1m" + "-".repeat(20);
                String reset = "\u001B[0m";  // Reset all formatting
                String bold = "\u001B[1m";   // Bold text
                String green = "\u001B[32m"; // Green text

                System.out.println(separator);
                System.out.println(bold + green + message + reset);
                System.out.println(separator);
                String input = scanner.nextLine().trim();

                if(input.contains("create-agent")) {
                    createAgent(input);
                }

                if(input.equals("status")) {
                    status();
                }

                if(input.equals("thread-status")) {
                    threadStatus();
                }

                if(input.contains("send-message")) {
                    sendMessage(input);
                }

                if(input.contains("move-agent")) {
                    moveAgent(input);
                }

                if(input.contains("stop-agent")) {
                    stopAgent(input);
                }

                if(input.equals("stop-agency")) {
                    exitRequested = true;
                }
            } catch (Exception e){
                System.out.println("------- ERRO -------\n" + e.getMessage());
            }
        }
        scanner.close();
    }

    // Método para parada de agência
    private void stopAgency() throws RemoteException {
        System.out.printf("[%s] - Iniciando processo de desligamento da agência\n", getAgencyName());
        var map = new HashMap<>(agentsThreads);
        // Itera sobre todos agentes na agência, desativando suas threads e removendo do servidor de nomes
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

    // Método para parar um agente específico
    private void stopAgent(String input) throws RemoteException {
        // Trata input para coletar dados necessários
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

    // Método para criar agente
    public void createAgent(String input) throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

        // Cria e executa thread para o novo agente
        Thread newAgentThread = new Thread(agent);
        newAgentThread.start();

        // Atualiza atributos de controle
        agentsThreads.put(agentName, newAgentThread);
        agentsList.add(agent);
        nameServer.associateAgentWithAgency(agentName, agencyName);
        System.out.printf("[%s] - Rodando agente: %s\n", this.agencyName, agent.agentName);
    }

    // Método para tratamento de dados quando mover agente para outra agência
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

    // Método para mover agente para outra agência
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

    // Método que informa agência atual de cada agente
    private void status() throws RemoteException {
        var map = nameServer.getMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Agent: " + key + ", Agency: " + value);
        }
    }

    // Método que informa threads em execução e seus respectivos agentes
    private void threadStatus() {
        for (Map.Entry<String, Thread> entry : agentsThreads.entrySet()) {
            String key = entry.getKey();
            Thread thread = entry.getValue();
            System.out.println("Agent: " + key + ", Thread: " + thread);
        }
    }
}
