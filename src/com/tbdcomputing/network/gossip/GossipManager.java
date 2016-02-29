package com.tbdcomputing.network.gossip;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.utils.GossipListUtils;
import org.json.JSONArray;

/**
 * This manager class will keep track of the GossipNodes in the network.
 */
public class GossipManager {
	private List<GossipNode> nodes;
    private HashMap<String, GossipNode> nodeMap;
	private GossipNode me;

	public GossipManager() {
		nodes = Collections.synchronizedList(new ArrayList<GossipNode>());
        nodeMap = new HashMap<String, GossipNode>();
        me = new GossipNode();
		nodes.add(me);
        nodeMap.put(me.getUUID(), me);

	}

	public GossipNode getMe() {
        return me;
	}

    /**
     * Receive a list of nodes, update our nodes accordingly and send back a list of nodes to update the sender with.
     */
    public void receiveNodeList() {
        try {
            // Create a socket and allow reuse.
            DatagramSocket socket = new DatagramSocket(Constants.GOSSIP_RECEIVE_PORT);
            socket.setReuseAddress(true);

            // Receive the list of nodes.
            byte[] buf = new byte[10240];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            // Get the node list out of received packet.
            String data = new String(packet.getData(), 0, packet.getLength());
            JSONArray receivedNodes = new JSONArray(data);

            // Update our list with the received list
            ArrayList<GossipNode> otherNodes = new ArrayList<GossipNode>();

            for(int i = 0; i < receivedNodes.length(); i++) {
                otherNodes.add((GossipNode)receivedNodes.get(i));
            }

            // Generate the diff list of nodes.
            List<GossipNode> diffNodes = GossipListUtils.generateDiffList(nodes, otherNodes, nodeMap);

            // Merge our list with the diff list.
            GossipListUtils.mergeList(nodes, diffNodes, nodeMap);

            // Send the diff list back to the other node.
            JSONArray json = new JSONArray(diffNodes);
            buf = json.toString().getBytes();

            socket.send(new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort()));

            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Send our list of nodes to a random other node.
	 */
	public void sendNodeList() {
		GossipNode other = pickPartner();

		try {
			DatagramSocket socket = new DatagramSocket(Constants.GOSSIP_PORT, other.getAddr());
			DatagramPacket packet;
            byte[] buf;

			// Serialize and send our entire list of nodes
            JSONArray json = new JSONArray(nodes);
            buf = json.toString().getBytes();
            packet = new DatagramPacket(buf, buf.length);

            socket.send(packet);

			// Wait to receive a diff list back
            byte[] recvBuf = new byte[10240];
            DatagramPacket received = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(received);

            String data = new String(received.getData(), 0, received.getLength());
            JSONArray receivedNodes = new JSONArray(data);
			
			// Update our list with the received list
            ArrayList<GossipNode> otherNodes = new ArrayList<GossipNode>();

            for(int i = 0; i < receivedNodes.length(); i++) {
                otherNodes.add((GossipNode)receivedNodes.get(i));
            }

            // Merge node lists.
            GossipListUtils.mergeList(nodes, otherNodes, nodeMap);

            socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }

    }
	
	/**
	 * Returns a random node from the list of nodes.
	 * @return A GossipNode from the nodes list. 
	 */
	public GossipNode pickPartner() {
		int index = (int) Math.floor((Math.random() * nodes.size()));
		return nodes.get(index);
	}

    /**
     * Get a node by its uuid.
     * @param uuid The UUID to search for.
     * @return  A GossipNode that contains the given UUID, null if not found.
     */
    public GossipNode getGossipNodeByUUID(String uuid) {
        return nodeMap.get(uuid);
    }

    /**
     * Add a node to the list of nodes and the map.
     * @param node  The node to add.
     */
    public void addNode(GossipNode node) {
        nodes.add(node);
        nodeMap.put(node.getUUID(), node);
    }

    /**
     * Remove a node by it's UUID.
     * @param uuid  The UUID corresponding to the removed node.
     */
    public void removeNodeByUUID(String uuid) {
        GossipNode node = nodeMap.remove(uuid);
        nodes.remove(node);
    }



}
