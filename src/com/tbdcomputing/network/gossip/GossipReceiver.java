package com.tbdcomputing.network.gossip;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.utils.GossipListUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 3/1/16.
 */
public class GossipReceiver {

    private GossipManager manager;

    public GossipReceiver(GossipManager manager) {
        this.manager = manager;
    }

    /**
     * Receive a list of nodes, update our nodes accordingly and send back a list of nodes to update the sender with.
     */
    public void receiveNodeList() {
        try {
            // Create a socket and allow reuse.
            DatagramSocket socket = new DatagramSocket(Constants.GOSSIP_RECEIVE_PORT);
            socket.setReuseAddress(true);

            List<GossipNode> nodes = manager.getNodes();
            Map<String, GossipNode> nodeMap = manager.getNodeMap();

            // Receive the list of nodes.
            byte[] buf = new byte[10240];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            // Get the node list out of received packet.
            String data = new String(packet.getData(), 0, packet.getLength());
            JSONArray receivedNodes = new JSONArray(data);
            System.out.println("receivedNodes: " + receivedNodes);

            // Update our list with the received list
            ArrayList<GossipNode> otherNodes = new ArrayList<GossipNode>();

            for (int i = 0; i < receivedNodes.length(); i++) {
                otherNodes.add(new GossipNode(receivedNodes.getJSONObject(i)));
            }

            // Generate the diff list of nodes.
            List<GossipNode> diffNodes = GossipListUtils.generateDiffList(nodes, otherNodes, nodeMap);

            // Merge our list with the diff list.
            GossipListUtils.mergeList(nodes, diffNodes, nodeMap);

            // Send the diff list back to the other node.
            JSONArray json = new JSONArray(diffNodes.parallelStream().map(GossipNode::toJSON).toArray());
            buf = json.toString().getBytes();

            socket.send(new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort()));

            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
