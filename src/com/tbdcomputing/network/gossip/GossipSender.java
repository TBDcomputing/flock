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

/**
 * Created by drew on 3/1/16.
 */
public class GossipSender {

    private GossipManager manager;

    public GossipSender(GossipManager manager) {
        this.manager = manager;
    }

    public void gossip() {
        System.out.println("Gossiping: " + manager.getNodes());

        GossipNode me = manager.getMe();

        me.setHeartbeat(me.getHeartbeat() + 1);
        GossipNode other = manager.gossipListener.onPickPartner(manager.getNodes());
        sendNodeList(other);
    }

    /**
     * Send our list of nodes to a random other node.
     */
    public void sendNodeList(GossipNode other) {

        try {
            DatagramSocket socket = new DatagramSocket(Constants.GOSSIP_PORT, other.getAddr());
            DatagramPacket packet;
            byte[] buf;

            List<GossipNode> nodes = manager.getNodes();
            HashMap<String, GossipNode> nodeMap = manager.getNodeMap();

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

            for (int i = 0; i < receivedNodes.length(); i++) {
                otherNodes.add((GossipNode) receivedNodes.get(i));
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
     *
     * @return A GossipNode from the nodes list.
     */
//    public GossipNode pickPartner() {
//        List<GossipNode> nodes = manager.getNodes();
//
//        int index = (int) Math.floor((Math.random() * nodes.size()));
//        return nodes.get(index);
//    }
}
