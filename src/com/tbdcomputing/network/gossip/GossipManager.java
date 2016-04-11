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
public class GossipManager extends Observable {
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
            notifyObservers(node);
        }
    }

    /**
     * Remove a node by it's UUID.
     *
     * @param uuid The UUID corresponding to the removed node.
     */
    public synchronized void removeNodeByUUID(String uuid) {
        // TODO: fire observable event
        GossipNode node = nodeMap.remove(uuid);
        nodes.remove(node);
    }

    public synchronized void removeAll(Collection<GossipNode> nodesToRemove) {
        // TODO: fire observable event
        nodes.removeAll(nodesToRemove);
        nodesToRemove.stream().forEach(node -> nodeMap.remove(node.getUUID()));
    }

    public synchronized List<GossipNode> getNodes() {
        return nodes;
    }

    public synchronized Map<String, GossipNode> getNodeMap() {
        return nodeMap;
    }

    /**
     * Merge the other list of @GossipNode in to our list of gossip nodes comparing based on generation time and heartbeat data.
     * @param otherList
     */
    public void mergeList(List<GossipNode> otherList) {
        for(GossipNode node: otherList) {
            if (node.getUUID().equals(Constants.getUUID())) {
                continue;
            }

            // Find corresponding node in our list
            GossipNode ourNode = nodeMap.get(node.getUUID());

            if(ourNode != null) {
                // If it is in our list, choose the more up to date one
                // If our generation time is higher, then just keep our copy.
                if(ourNode.getGenerationTime() < node.getGenerationTime()) {
                    // The node went down, and other node is more up to date.
                    ourNode.update(node);
                } else if(ourNode.getGenerationTime() == node.getGenerationTime()) {
                    // The node never went down, check heart beat
                    // keep ours if heartbeats are the same or ours is greater.
                    if(ourNode.getHeartbeat() < node.getHeartbeat()) {
                        ourNode.update(node);
                    }
                }

            } else {
                // It is not in our list, so add it to our list.
                addNode(node);
            }

        }
    }

    /**
     * Generate the list of all nodes that differ between two lists.
     * @param otherList The list of nodes to compare to.
     * @return          A list of all nodes that differ between the two lists.
     */
    public List<GossipNode> generateDiffList(List<GossipNode> otherList) {
        List<GossipNode> diffNodes = new ArrayList<GossipNode>();

        for(GossipNode node : otherList) {
            GossipNode ourNode = nodeMap.get(node.getUUID());

            if(ourNode != null) {
                if(ourNode.getGenerationTime() < node.getGenerationTime()) {
                    // The node went down, and other node is more up to date.
                    diffNodes.add(node);
                } else if(ourNode.getGenerationTime() == node.getGenerationTime()) {
                    // The node never went down, check heart beat
                    // keep ours if heartbeats are the same or ours is greater.
                    if(ourNode.getHeartbeat() < node.getHeartbeat()) {
                        diffNodes.add(node);
                    } else if (ourNode.getHeartbeat() > node.getHeartbeat()) {
                        diffNodes.add(ourNode);
                    }
                } else {
                    diffNodes.add(ourNode);
                }
            } else {
                diffNodes.add(node);
            }

        }

        for(GossipNode node : nodes) {
            if(!diffNodes.contains(node)) {
                diffNodes.add(node);
            }
        }

        return diffNodes;
    }


}
