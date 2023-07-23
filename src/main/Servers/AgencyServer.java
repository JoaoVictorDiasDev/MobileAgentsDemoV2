package main.Servers;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class AgencyServer {
    public static void main(String[] args) {
        createAgencyRegistry();
    }

    private static void createAgencyRegistry() {
        try
        {
            String message = "Criando servidor de agências";
            String separator = "\u001B[1m" + "-".repeat(message.length());

            String reset = "\u001B[0m";  // Reset all formatting
            String bold = "\u001B[1m";   // Bold text
            String green = "\u001B[32m"; // Green text

            System.out.println(separator);
            System.out.println(bold + green + message + reset);
            System.out.println(separator);

            Scanner scanner = new Scanner(System.in);

            System.out.println("Digite a porta do servidor de agências");
            int agencyServerPort = Integer.parseInt(scanner.nextLine().trim());

            LocateRegistry.createRegistry(agencyServerPort);
            System.out.println("Servidor de agências criado com sucesso");
            while(true){}
        }
        catch (Exception e)
        {
            System.out.println("Ocorreu um problema na criação do servidor de agências\n"+e.toString());
        }
    }
}
