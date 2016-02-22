package com.tbdcomputing.network.discovery;

import java.net.DatagramPacket;

public interface NetworkDiscoveryListener {
    void onNodeDiscovered(DatagramPacket packet);
}
