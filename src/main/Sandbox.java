package main;

public class Sandbox implements Runnable{
    private Agent agent;
    private Thread controlThread;

    public Sandbox(Agent agent) {
        this.agent = agent;
        this.controlThread = new Thread(this);
    }

    @Override
    public void run() {
        agent.run();
    }

    public void start() {
        controlThread.start();
    }
}
