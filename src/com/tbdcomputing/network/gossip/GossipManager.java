package com.tbdcomputing.network.gossip;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.tbdcomputing.network.Constants;
import org.json.JSONArray;

public class GossipManager {
	private List<GossipNode> nodes;
	private GossipNode me;

	public GossipManager() {
		nodes = Collections.synchronizedList(new ArrayList<GossipNode>());
		
		try {
			me = new GossipNode(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		nodes.add(me);
	}

	public void run() {

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
            ArrayList<GossipNode> diffNodes = new ArrayList<GossipNode>();

            for(int i = 0; i < receivedNodes.length(); i++) {
                diffNodes.add((GossipNode)receivedNodes.get(i));
            }

		
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
}
