package com.tbdcomputing.network.gossip;

import java.util.List;

/**
 * Created by akatkov on 3/2/16.
 */
public interface GossipListener {
    GossipNode onPickPartner(List<GossipNode> nodes);
}
