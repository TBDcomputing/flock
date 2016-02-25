package com.tbdcomputing.network.discovery;

import com.tbdcomputing.network.Constants;

import java.io.IOException;
import java.net.*;

/**
 * This class listens for UDP broadcasts from any clients interested in gathering this node's information. It responds
 * according to the behavior determined by the listener allowing us to customize the behavior easily.
 */
public class NetworkDiscoveryReceiver extends Thread implements Runnable {

    private NetworkDiscoveryListener listener;
    private DatagramSocket socket;

    public NetworkDiscoveryReceiver(NetworkDiscoveryListener listener){
        super();
        this.listener = listener;
        // create the listening socket on our predetermined port
        try {
            this.socket = new DatagramSocket(Constants.NETWORK_DISCOVERY_PORT);
            this.socket.setReuseAddress(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    /**
     * Listens for a broadcast, and calls onNodeDiscovered to determine the response behavior.
     */
    public void run() {
        try {
            //TODO why isn't this in a while loop whereas it's cancellable thread is in a while loop? More importantly, we never close() socket....
            // TODO: set max size for buf in Constants, and use it here


            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            listener.onNodeDiscovered(packet);
        }  catch (SocketException e) {
            System.out.println("socket exception ignoring for now see TODO");
            //TODO receiver interrupt from 'quit' cmd throws a socket exception because of socket.close how to fix this?
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("regularexceptionxyz");
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
//        socket.disconnect(); //this just blocks forever, no idea
        super.interrupt();
        socket.close();

    }
}
