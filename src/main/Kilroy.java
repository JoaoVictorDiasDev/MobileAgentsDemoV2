package main;

public class Kilroy extends Agent {

    public Kilroy(String name) {
        super(name);
    }


    @Override
    public void onArrival() {

/*
        Frame f;
        f = new Frame("Kilroy");
        f.setLayout(new BorderLayout());
        f.add(new Label("Kilroy" + agentName + "Was Here!", Label.CENTER));
        f.setSize(400,50);
        f.show();
        f.toFront();
*/

        System.out.println("Kilroy " + agentName + " Was in agency: " + currentAgency.agencyName);
        try { Thread.sleep(5000); } catch (InterruptedException ie) {};
        System.out.println("Kilroy " + agentName + " Stopped Sleeping");
        //f.dispose();

    }
}
