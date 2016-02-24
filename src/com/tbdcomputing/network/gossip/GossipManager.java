package com.tbdcomputing.network.gossip;

import java.util.ArrayList;

public class GossipManager {
    private ArrayList<GossipNode> nodes;
    private GossipNode me;

    public GossipManager() {
        me = new GossipNode();
        nodes = new ArrayList<>();
        nodes.add(me);
    }
}
