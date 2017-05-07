package plu.teamtwo.rtm.expiriments;

import plu.teamtwo.rtm.neat.Encoding;
import plu.teamtwo.rtm.neat.NEATController;
import py4j.GatewayServer;

public class PythonServer {
    NEATController controller;

    public PythonServer() {
        controller = null;
    }

    public void init(Encoding encoding, int inputs, int outputs) {
        controller = new NEATController(encoding, inputs, outputs);
    }

    public NEATController getController() {
        return controller;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new PythonServer());
        gatewayServer.start();
        System.out.println("Gateway server started...");
    }
}
