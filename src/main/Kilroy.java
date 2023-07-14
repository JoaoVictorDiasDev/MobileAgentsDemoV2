package main;

import java.awt.*;

public class Kilroy extends Agent {

    public Kilroy(String home) {
        super(home);
    }

    @Override
    public void beforeDeparture() {
        goTo("localhost");
    }

    @Override
    public void onArrival() {
        Frame f;
        f = new Frame("Kilroy");
        f.setLayout(new BorderLayout());
        f.add(new Label("Kilroy Was Here!", Label.CENTER));
        f.setSize(400,50);
        f.show();
        f.toFront();

        try { Thread.sleep(5000); } catch (InterruptedException ie) {};
        f.dispose();

    }

    @Override
    public void onReturn() {

    }
}
