package com.tbdcomputing.network.leaderelection;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipNode;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends messages to other servers.
 * <p>
 * Created by dpho on 3/18/16.
 */
public class ElectionSender {
    private final Logger log = Logger.getLogger(ElectionSender.class.getName());
    DatagramSocket socket;

    public ElectionSender() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            log.log(Level.SEVERE, e.toString(), e);
        }
    }

    public void sendMessage(JSONObject msg, InetAddress dst) {
        try {
            byte[] buf = msg.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, dst, Constants.ELECTION_RECEIVE_PORT);
            socket.send(packet);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.toString(), e);
        }
    }

    public void broadcast(JSONObject msg, List<GossipNode> cluster) {
        for (GossipNode node : cluster) {
            sendMessage(msg, node.getAddr());
        }
    }
}
