package plu.teamtwo.rtm.experiments;

import plu.teamtwo.rtm.neat.Encoding;
import plu.teamtwo.rtm.neat.GAController;
import py4j.GatewayServer;

public class PythonServer {
    GAController controller;

    public PythonServer() {
        controller = null;
    }

    public void init(Encoding encoding, int inputs, int outputs) {
        controller = new GAController(encoding, inputs, outputs);
    }

    public GAController getController() {
        return controller;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new PythonServer());
        gatewayServer.start();
        System.out.println("Gateway server started...");
    }
}
