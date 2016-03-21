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

    public NetworkDiscoveryReceiver(NetworkDiscoveryListener listener){
        super();
        this.listener = listener;
    }


    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Listens for a broadcast, and calls onNodeDiscovered to determine the response behavior.
     */
    public void run() {
        socket = null;
        try {
            socket = new DatagramSocket(Constants.NETWORK_DISCOVERY_PORT);
            socket.setReuseAddress(true);
            //TODO: set max size for buf in Constants, and use it here
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            listener.onNodeDiscovered(packet);
            //TODO why aren't we just closing the socket here instead of using interrupt signals, or why do we do it the other way in broadcaster?
        } catch (SocketTimeoutException ste) {
            //TODO log at warning level once a logger is implemented
        } catch (SocketException e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
            socket = null;
        }
    }
}
