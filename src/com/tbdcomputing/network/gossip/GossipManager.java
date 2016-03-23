package com.tbdcomputing.network.gossip;

import java.io.IOException;
import java.net.*;
import java.util.*;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.utils.GossipListUtils;
import org.json.JSONArray;

/**
 * This manager class will keep track of the GossipNodes in the network.
 */
public class GossipManager {
    private List<GossipNode> nodes;
    private Map<String, GossipNode> nodeMap;
    private GossipNode me;
    public GossipListener gossipListener;

    public GossipManager(GossipListener gossipListener) {
        nodes = Collections.synchronizedList(new ArrayList<GossipNode>());
        nodeMap = Collections.synchronizedMap(new HashMap<String, GossipNode>());
        me = new GossipNode();
        try {
            me.setAddr(Constants.findLocalAddress());
        } catch (SocketException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't find your local address!");
        }
//        nodes.add(me);
//        nodeMap.put(me.getUUID(), me);

        this.gossipListener = gossipListener;
    }

    public synchronized GossipNode getMe() {
        return me;
    }

    /**
     * Get a node by its uuid.
     *
     * @param uuid The UUID to search for.
     * @return A GossipNode that contains the given UUID, null if not found.
     */
    public synchronized GossipNode getGossipNodeByUUID(String uuid) {
        return nodeMap.get(uuid);
    }

    /**
     * Add a node to the list of nodes and the map.
     *
     * @param node The node to add.
     */
    public synchronized void addNode(GossipNode node) {
        if (!nodeMap.containsKey(node.getUUID())) {
            nodes.add(node);
            nodeMap.put(node.getUUID(), node);
        }
    }

    /**
     * Remove a node by it's UUID.
     *
     * @param uuid The UUID corresponding to the removed node.
     */
    public synchronized void removeNodeByUUID(String uuid) {
        GossipNode node = nodeMap.remove(uuid);
        nodes.remove(node);
    }

    public synchronized void removeAll(Collection<GossipNode> nodesToRemove) {
        nodes.removeAll(nodesToRemove);
        nodesToRemove.stream().forEach(node -> nodeMap.remove(node.getUUID()));
    }

    public synchronized List<GossipNode> getNodes() {
        return nodes;
    }

    public synchronized Map<String, GossipNode> getNodeMap() {
        return nodeMap;
    }


}
