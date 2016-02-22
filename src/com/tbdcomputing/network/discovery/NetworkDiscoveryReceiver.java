package com.tbdcomputing.network.discovery;

import com.tbdcomputing.network.Constants;

import java.io.IOException;
import java.net.*;

/**
 * This class listens for UDP broadcasts from any clients interested in gathering this node's information. It responds
 * with the listener's IP address and any other relevant information for the purpose of initiating gossip on the
 * broadcasting node.
 */
public class NetworkDiscoveryReceiver implements Runnable {

    private NetworkDiscoveryListener listener;
    private DatagramSocket socket;

    public NetworkDiscoveryReceiver(NetworkDiscoveryListener listener) throws SocketException {
        super();
        this.listener = listener;
        this.socket = new DatagramSocket(Constants.PORT);
        this.socket.setReuseAddress(true);
    }

    /**
     * Listens for a broadcast, and then responds with the listener's IP address and any other relevant gossip
     * information.
     */
    public void run() {
        try {
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            listener.onNodeDiscovered(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
