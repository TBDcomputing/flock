package com.tbdcomputing.network.discovery;

import java.net.DatagramPacket;

/**
 * This interface determines what should be done when a node is discovered. There will be more methods in the future
 * in order to modify behavior during gossip. Possibilities include onGossipStart(), onGossipEnd(), and/or others.
 */
public interface NetworkDiscoveryListener {
    /*
     * Called whenever a node is discovered by the NetworkDiscoveryReceiver
     */
    void onNodeDiscovered(DatagramPacket packet);
}
