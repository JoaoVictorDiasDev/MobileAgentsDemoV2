package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("resource")
public class SandboxServer {
    private static final int    PORT = 9200;
    private static final String EXT = ".class";

    public static void main(String[] args) {
        Agent               agent;
        byte[]              byteCodes;
        Class               agentClass;
        FileOutputStream    out;
        ObjectInputStream   in;
        Sandbox             sandbox;
        ServerSocket        ss;
        Socket              s;
        String              agentName;

        try {
            ss = new ServerSocket(PORT);
            while (true) {
                s = ss.accept();
                in = new ObjectInputStream(s.getInputStream());

                agentName = (String) in.readObject();

                byteCodes = (byte[]) in.readObject();

                out = new FileOutputStream(agentName+".class");
                out.write(byteCodes);

                agent = (Agent)in.readObject();

                sandbox = new Sandbox(agent);
                sandbox.start();
            }
        } catch (IOException e) {
            System.err.println("Erro IO");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.err.println("Erro Classe Nao Encontrada");
            throw new RuntimeException(e);
        }
    }
}
