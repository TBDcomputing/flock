package com.tbdcomputing.network;

import com.tbdcomputing.network.discovery.NetworkDiscoveryBroadcaster;
import com.tbdcomputing.network.discovery.NetworkDiscoveryListener;
import com.tbdcomputing.network.discovery.NetworkDiscoveryReceiver;
import com.tbdcomputing.network.gossip.*;
import com.tbdcomputing.network.leaderelection.ElectionManager;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * The main class for the Flock software. This class should be run in order to begin the program. It will begin
 * listening for new nodes as well as broadcast its information every once in a while. Probably will also have
 * some input processing ala interactive mode so that the user can interact with the Flock.
 * <p>
 * Created by akatkov on 2/22/16.
 */
public class Flock {

    private static boolean running = false;

    private static Thread gossipReceiverThread;
    private static Thread gossipSenderThread;
    private static GossipListener randomListener = new GossipListener() {

        private GossipNode lastSelectedNode;

        @Override
        public GossipNode onPickPartner(List<GossipNode> nodes) {
            // only pick nodes to gossip with normal status
            List<GossipNode> viableNodes = nodes.stream()
                    .filter(node -> node.getStatus() == GossipStatus.NORMAL)
                    .collect(Collectors.toList());
            if (!viableNodes.isEmpty()) {
                // attempt to not select the same node as last time
                int index = (int) Math.floor((Math.random() * viableNodes.size()));
                if (viableNodes.get(index) == lastSelectedNode) {
                    index = (index + 1) % viableNodes.size();
                }
                lastSelectedNode = viableNodes.get(index);
                System.out.println("Selected (" + viableNodes.get(index).toString() + ") for gossip");
                return viableNodes.get(index);
            }
            return null;
        }
    };
    // stores information about the nodes including itself
    private static GossipManager manager = new GossipManager(randomListener);
    private static GossipReceiver gossipReceiver = new GossipReceiver(manager);
    private static GossipSender gossipSender = new GossipSender(manager);

    private static NetworkDiscoveryListener basicListener = new NetworkDiscoveryListener() {
        @Override
        public void onNodeDiscovered(DatagramPacket packet) {
            // parse packet as JSON data
            GossipNode node = new GossipNode(new JSONObject(new String(packet.getData(), 0, packet.getLength())));
            if (node.getUUID().equals(Constants.getUUID())) {
                System.out.println("Discovered self...");
            } else {
                System.out.println("Other node: " + node);
                manager.addNode(node);
            }
        }
    };

    private static Thread receiverThread;
    private static NetworkDiscoveryReceiver receiver;
    private static Thread broadcasterThread;
    private static NetworkDiscoveryBroadcaster broadcaster;

    private static ElectionManager election;

    public static void main(String[] args) {

        //TODO add a startup command probably with apache cli and then also a preferences object
        Scanner cmdLine = new Scanner(System.in);
        while (true) {
            String cmd = cmdLine.nextLine();
            if (cmd.toLowerCase().equals("start") && !running) {
                running = true;
                run();
            } else if (cmd.toLowerCase().equals("quit") && running) {
                System.out.println("system shutting down...");
                manager.getMe().setStatus(GossipStatus.LEAVING);
                gossipSender.gossip();

                // Stop the cancellable thread wrappers and then join them
                broadcasterThread.interrupt();
                receiverThread.interrupt();
                // we close the socket so that the interrupt will succeed
                if (receiver.getSocket() != null) {
                    receiver.getSocket().close();
                }
                gossipSenderThread.interrupt();
                gossipReceiverThread.interrupt();
                // stopping leader election
                if (election != null) {
                    election.interrupt();
                }
                // we close the socket so that the interrupt will succeed
                if (gossipReceiver.getSocket() != null) {
                    gossipReceiver.getSocket().close();
                }
                try {
                    receiverThread.join();
                    broadcasterThread.join();
                    gossipSenderThread.join();
                    gossipReceiverThread.join();
                    if (election != null) {
                        election.join();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("System shutdown complete, later tater!");
                break;
            } else if (cmd.toLowerCase().equals("elect") && running) {
                startElection();
            }
        }
    }

    private static void startElection() {
        election = new ElectionManager(manager);
        election.start();
    }

    private static void run() {
        // set up the receiver with the listener
        // throws a SocketException if we can't bind to the port
        receiver = new NetworkDiscoveryReceiver(basicListener);
        // continually listen for new nodes
        receiverThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    receiver.run();
                }
                System.err.println("Exiting receiver thread!");
            }
        };
        receiverThread.start();

        broadcaster = new NetworkDiscoveryBroadcaster(manager.getMe());
//         broadcast our existence every 10 seconds
        broadcasterThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    broadcaster.run();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                System.err.println("Exiting broadcaster thread!");
            }
        };
        broadcasterThread.start();

        gossipReceiverThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    gossipReceiver.receiveNodeList();
                }
                System.err.println("Exiting gossip receiver thread!");
            }
        };
        gossipReceiverThread.start();

        gossipSenderThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    gossipSender.gossip();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                System.err.println("Exiting gossip sender thread!");
            }
        };
        gossipSenderThread.start();
    }

}
