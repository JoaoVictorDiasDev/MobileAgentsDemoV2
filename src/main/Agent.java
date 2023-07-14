package main;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

public abstract class Agent implements Runnable, Serializable {

    private byte[]              byteCodes;
    private LinkedList<String>  hosts;
    private String              home;

    public static final int     PORT = 9200;
    public static final String  EXT = ".class";

    public Agent(String home) {
        FileInputStream in;
        byteCodes = null;

        try {
            in = new FileInputStream(getName()+EXT);
            byteCodes = new byte[in.available()];
            in.read(byteCodes);

        } catch (FileNotFoundException e) {
            System.err.println("Excecao File Not Found");
            throw new RuntimeException(e);
        } catch (IOException e) {
            byteCodes = null;
            System.err.println("Excecao IO");
            throw new RuntimeException(e);
        }
        this.home = home;
    }

    public void addHost(String host) {
        hosts.addFirst(host);
    }

    public void goTo(String host){
        ObjectOutputStream   out;
        Socket              s;

        try {

            s = new Socket(host, PORT);
            out = new ObjectOutputStream(s.getOutputStream());

            out.writeObject(getName());
            out.writeObject(getByteCodes());
            out.writeObject(this);

        } catch (IOException e) {
            host = hosts.getFirst();
            hosts.removeFirst();
            if(host != null) goTo(host);
        }
    }

    public abstract void beforeDeparture();
    public abstract void onArrival();
    public abstract void onReturn();

    public byte[] getByteCodes() {
        return byteCodes;
    }

    private String getName() {
        return getClass().getName();
    }

    @Override
    public void run() {
        String host;
        if(hosts == null){
            hosts = new LinkedList<>();
            hosts.addLast(home);
            beforeDeparture();
        }
        else if (hosts.size() == 0)
        {
            onReturn();
        }
        else
        {
            onArrival();
            host = hosts.getFirst();
            hosts.removeFirst();
            goTo(host);
        }
    }
}
