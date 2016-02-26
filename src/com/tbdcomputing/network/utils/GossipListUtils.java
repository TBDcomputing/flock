package com.tbdcomputing.network.utils;

import com.tbdcomputing.network.gossip.GossipNode;

import java.util.List;

/**
 * Created by drew on 2/26/16.
 */
public class GossipListUtils {
    /**
     * Merge the other list of @GossipNode in to our list of gossip nodes comparing based on generation time and heartbeat data.
     * @param otherList
     */
    public void mergeList(List<GossipNode> ourList, List<GossipNode> otherList) {
        for(GossipNode node: otherList) {
            // Find corresponding node in our list
            GossipNode ourNode = findNodeByUUID(node.getUUID(), ourList);

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
                ourList.add(node);
            }

        }
    }


    // TODO: create GossipList class which has a HashTable mapping UUID to nodes in our synchronized arraylist.  This allows us to search very efficiently and get nodes from our list in constant time for updates, etc.

    /**
     * Finds the node with the following UUID in our list of nodes.
     * @param uuid  The UUID to search for.
     * @param ourList   The list to search in.
     * @return  The node that possesses that uuid or null if it is not in the list.
     */
    public GossipNode findNodeByUUID(String uuid, List<GossipNode> ourList) {
        for(GossipNode node : ourList) {
            if(uuid.equals(node.getUUID())) {
                return node;
            }
        }
        return null;
    }

}
