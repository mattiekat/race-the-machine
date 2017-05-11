package plu.teamtwo.rtm.expiriments;

import py4j.GatewayServer;

import java.util.LinkedList;
import java.util.List;

/**
 * To use this test server, you can run the following python code while it is running.
 * <p>
 * <code>
 * from py4j.java_gateway import JavaGateway()
 * gateway = JavaGateway()
 * stack = gateway.entry_point.getStack()
 * #now you can use stack function calls
 * </code>
 */
public class PythonServerTest {
    private Stack stack;


    public PythonServerTest() {
        stack = new Stack();
        stack.push("Initial Item");
    }


    public Stack getStack() {
        return stack;
    }


    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new PythonServerTest());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }
}



class Stack {
    private List<String> internalList = new LinkedList<String>();


    public void push(String element) {
        internalList.add(0, element);
    }


    public String pop() {
        return internalList.remove(0);
    }


    public List<String> getInternalList() {
        return internalList;
    }


    public void pushAll(List<String> elements) {
        for(String element : elements)
            this.push(element);
    }
}