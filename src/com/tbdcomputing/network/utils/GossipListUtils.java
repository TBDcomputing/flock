package com.tbdcomputing.network.utils;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 2/26/16.
 */
public class GossipListUtils {
    /**
     * Merge the other list of @GossipNode in to our list of gossip nodes comparing based on generation time and heartbeat data.
     * @param otherList
     */
    public static void mergeList(List<GossipNode> ourList, List<GossipNode> otherList, Map<String, GossipNode> map) {
        for(GossipNode node: otherList) {
            if (node.getUUID().equals(Constants.getUUID())) {
                continue;
            }

            // Find corresponding node in our list
            GossipNode ourNode = map.get(node.getUUID());

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
                map.put(node.getUUID(), node);
            }

        }
    }

    /**
     * Generate the list of all nodes that differ between two lists.
     * @param ourList   Our list of nodes.
     * @param otherList The list of nodes to compare to.
     * @return          A list of all nodes that differ between the two lists.
     */
    public static List<GossipNode> generateDiffList(List<GossipNode> ourList, List<GossipNode> otherList, Map<String, GossipNode> map) {
        List<GossipNode> nodes = new ArrayList<GossipNode>();

        for(GossipNode node : otherList) {
            GossipNode ourNode = map.get(node.getUUID());

            if(ourNode != null) {
                if(ourNode.getGenerationTime() < node.getGenerationTime()) {
                    // The node went down, and other node is more up to date.
                    nodes.add(node);
                } else if(ourNode.getGenerationTime() == node.getGenerationTime()) {
                    // The node never went down, check heart beat
                    // keep ours if heartbeats are the same or ours is greater.
                    if(ourNode.getHeartbeat() < node.getHeartbeat()) {
                        nodes.add(node);
                    } else if (ourNode.getHeartbeat() > node.getHeartbeat()) {
                        nodes.add(ourNode);
                    }
                } else {
                    nodes.add(ourNode);
                }
            } else {
                nodes.add(node);
            }

        }

        for(GossipNode node : ourList) {
            if(!nodes.contains(node)) {
                nodes.add(node);
            }
        }

        return nodes;
    }


}
