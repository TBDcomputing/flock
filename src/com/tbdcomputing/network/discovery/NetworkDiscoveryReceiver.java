package com.tbdcomputing.network.discovery;

import com.tbdcomputing.network.Constants;

import java.io.IOException;
import java.net.*;

/**
 * This class listens for UDP broadcasts from any clients interested in gathering this node's information. It responds
 * according to the behavior determined by the listener allowing us to customize the behavior easily.
 */
public class NetworkDiscoveryReceiver implements Runnable {

    private NetworkDiscoveryListener listener;
    private DatagramSocket socket;

    public NetworkDiscoveryReceiver(NetworkDiscoveryListener listener) throws SocketException {
        super();
        this.listener = listener;
        // create the listening socket on our predetermined port
        this.socket = new DatagramSocket(Constants.NETWORK_DISCOVERY_PORT);
        this.socket.setReuseAddress(true);
    }

    /**
     * Listens for a broadcast, and calls onNodeDiscovered to determine the response behavior.
     */
    public void run() {
        try {
            // TODO: set max size for buf in Constants, and use it here
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            listener.onNodeDiscovered(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
