package com.tbdcomputing.network;

import com.tbdcomputing.network.discovery.NetworkDiscoveryBroadcaster;
import com.tbdcomputing.network.discovery.NetworkDiscoveryListener;
import com.tbdcomputing.network.discovery.NetworkDiscoveryReceiver;
import com.tbdcomputing.network.gossip.GossipNode;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * The main class for the Flock software. This class should be run in order to begin the program. It will begin
 * listening for new nodes as well as broadcast its information every once in a while. Probably will also have
 * some input processing ala interactive mode so that the user can interact with the Flock.
 *
 * Created by akatkov on 2/22/16.
 */
public class Flock {

    private static Thread receiverThread;
    private static NetworkDiscoveryListener basicListener = new NetworkDiscoveryListener() {
        @Override
        public void onNodeDiscovered(DatagramPacket packet) {
            // parse packet as JSON data
            GossipNode node = new GossipNode(new JSONObject(new String(packet.getData())));
            if (node.getUUID().equals(Constants.getUUID())) {
                System.out.println("Discovered self...");
            } else {
                System.out.println("Other node: " + node);
            }
        }
    };
    private static NetworkDiscoveryReceiver receiver;
    private static Thread broadcasterThread;
    private static NetworkDiscoveryBroadcaster broadcaster = new NetworkDiscoveryBroadcaster();

    public static void main(String[] args) {
        // set up the receiver with the listener
        // throws a SocketException if we can't bind to the port
        try {
            receiver = new NetworkDiscoveryReceiver(basicListener);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        // continually listen for new nodes
        receiverThread = new CancellableThread() {
            @Override
            public void run() {
                while (!isCancelled) {
                    receiver.run();
                }
            }
        };
        receiverThread.start();

        // broadcast our existence every 10 seconds
        broadcasterThread = new CancellableThread() {
            @Override
            public void run() {
                while (!isCancelled) {
                    broadcaster.run();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        broadcasterThread.start();

        // TODO: add way to cancel the above threads so that they can be joined below
        // probably via some interactive mode

        try {
            receiverThread.join();
            broadcasterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Small extension of Thread so that we can cancel the inner loop of run easily, and
     * let another thread join them.
     */
    private static class CancellableThread extends Thread {
        protected volatile boolean isCancelled = false;
        public void cancel() {
            isCancelled = true;
        }
    }
}
