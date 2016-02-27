package com.tbdcomputing.network;

import com.tbdcomputing.network.discovery.NetworkDiscoveryBroadcaster;
import com.tbdcomputing.network.discovery.NetworkDiscoveryListener;
import com.tbdcomputing.network.discovery.NetworkDiscoveryReceiver;
import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.gossip.GossipNode;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * The main class for the Flock software. This class should be run in order to begin the program. It will begin
 * listening for new nodes as well as broadcast its information every once in a while. Probably will also have
 * some input processing ala interactive mode so that the user can interact with the Flock.
 * <p>
 * Created by akatkov on 2/22/16.
 */
public class Flock {

    private static boolean running = false;

    // stores information about the nodes including itself
    private static GossipManager manager = new GossipManager();

    private static CancellableThread receiverThread;
    private static NetworkDiscoveryListener basicListener = new NetworkDiscoveryListener() {
        @Override
        public void onNodeDiscovered(DatagramPacket packet) {
            // parse packet as JSON data
            GossipNode node = new GossipNode(new JSONObject(new String(packet.getData(), 0, packet.getLength())));
            if (node.getUUID().equals(Constants.getUUID())) {
                System.out.println("Discovered self...");
            } else {
                System.out.println("Other node: " + node);
            }
        }
    };
    private static NetworkDiscoveryReceiver receiver;
    private static CancellableThread broadcasterThread;
    private static NetworkDiscoveryBroadcaster broadcaster = new NetworkDiscoveryBroadcaster(manager.getMe());

    public static void main(String[] args) {

        //TODO add a startup command probably with apache cli and then also a preferences object
        Scanner cmdLine = new Scanner(System.in);
        while (true) {
            String cmd = cmdLine.nextLine();
            if (cmd.toLowerCase().equals("start")) {
                running = true;
                run();
            } else if (cmd.toLowerCase().equals("quit") && running) {
                System.out.println("system shutting down...");
                // Stop the cancellable thread wrappers and then join them
                receiverThread.cancel();
                broadcasterThread.cancel();
                try {
                    receiverThread.join();
                    broadcasterThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("System shutdown complete, later tater!");
                break;
            }
        }
    }

    private static void run() {
        // set up the receiver with the listener
        // throws a SocketException if we can't bind to the port
        receiver = new NetworkDiscoveryReceiver(basicListener);
        // continually listen for new nodes
        receiverThread = new CancellableThread() {
            @Override
            public void run() {
                while (!isCancelled) {
                    receiver.run();
                }
                receiver.interrupt();
                try {
                    receiver.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        receiverThread.start();

//         broadcast our existence every 10 seconds
        broadcasterThread = new CancellableThread() {
            @Override
            public void run() {
                while (!isCancelled) {
                    broadcaster.run();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        broadcasterThread.start();
    }

    /**
     * Small extension of Thread so that we can cancel the inner loop of run easily, and
     * let another thread join them.
     *
     * Must join the underlying thread which this wraps around.
     */
    private static class CancellableThread extends Thread {
        protected volatile boolean isCancelled = false;

        public void cancel() {
            isCancelled = true;
        }
    }
}
