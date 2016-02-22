package com.tbdcomputing.network.discovery;

import java.net.DatagramPacket;

public interface NetworkDiscoveryListener {
    /*
     * Called whenever a node is discovered by the NetworkDiscoveryReceiver
     */
    void onNodeDiscovered(DatagramPacket packet);
}
