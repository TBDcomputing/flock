package com.tbdcomputing.network.gossip;

import java.util.ArrayList;

/**
 * This manager class will keep track of the GossipNodes in the network.
 */
public class GossipManager {
    private ArrayList<GossipNode> nodes;
    private GossipNode me;

    /**
     * Initializes the local GossipNode info and adds it the list of nodes they know.
     */
    public GossipManager() {
        me = new GossipNode();
        nodes = new ArrayList<>();
        nodes.add(me);
    }
}
