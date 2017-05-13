package plu.teamtwo.rtm.experiments;

import py4j.GatewayServer;

import java.util.LinkedList;
import java.util.List;

public class PythonInterfaceTest {
    private final List<Listener> listeners = new LinkedList<>();


    public void registerListener(Listener l) {
        listeners.add(l);
        System.out.println("Registered a listener");
    }


    public void notifyAllListeners(String message) {
        int x = 0;
        for(Listener l : listeners)
            System.out.println(l.notify(message));
    }


    public void duplicateAll() {
        LinkedList<Listener> dups = new LinkedList<>();
        for(Listener l : listeners)
            dups.add(l.duplicate());
        listeners.addAll(dups);
    }


    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new PythonInterfaceTest());
        gatewayServer.start();
        System.out.println("Gateway server started...");
    }


    public interface Listener {
        String notify(String input);
        Listener duplicate();
    }
}
