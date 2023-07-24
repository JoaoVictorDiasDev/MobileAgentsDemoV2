package main;

import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class AgencyServer {
    public static void main(String[] args) {
        createAgencyRegistry();
    }

    private static void createAgencyRegistry() {
        try
        {
            // Imprime header
            String message = "Criando servidor de agências";
            String separator = "\u001B[1m" + "-".repeat(message.length());

            String reset = "\u001B[0m";
            String bold = "\u001B[1m";
            String green = "\u001B[32m";

            System.out.println(separator);
            System.out.println(bold + green + message + reset);
            System.out.println(separator);

            Scanner scanner = new Scanner(System.in);

            // Pega dados para criação do registry
            System.out.println("Digite a porta do servidor de agências");
            int agencyServerPort = Integer.parseInt(scanner.nextLine().trim());

            // Cria registry
            LocateRegistry.createRegistry(agencyServerPort);
            System.out.println("Servidor de agências criado com sucesso");
            while(true){}
        }
        catch (Exception e)
        {
            System.out.println("Ocorreu um problema na criação do servidor de agências\n"+ e);
        }
    }
}
