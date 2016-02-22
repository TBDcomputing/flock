package com.tbdcomputing.network;

import com.tbdcomputing.network.discovery.NetworkDiscoveryBroadcaster;
import com.tbdcomputing.network.discovery.NetworkDiscoveryListener;
import com.tbdcomputing.network.discovery.NetworkDiscoveryReceiver;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by akatkov on 2/22/16.
 */
public class Flock {

    private static Thread receiverThread;
    private static NetworkDiscoveryListener basicListener = new NetworkDiscoveryListener() {
        @Override
        public void onNodeDiscovered(DatagramPacket packet) {
            // read the uuid in, store the info about this node
            System.out.println(String.format("Node Discovered: %s, Data: %s", packet.getAddress().getHostAddress(), new String(packet.getData())));
        }
    };
    private static NetworkDiscoveryReceiver receiver;
    private static Thread broadcasterThread;
    private static NetworkDiscoveryBroadcaster broadcaster = new NetworkDiscoveryBroadcaster();

    public static void main(String[] args) {
        try {
            receiver = new NetworkDiscoveryReceiver(basicListener);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        receiverThread = new CancellableThread() {
            @Override
            public void run() {
                while (!isCancelled) {
                    receiver.run();
                }
            }
        };
        receiverThread.start();

        broadcasterThread = new CancellableThread() {
            @Override
            public void run() {
                while (!isCancelled) {
                    broadcaster.run();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        broadcasterThread.start();

        try {
            receiverThread.join();
            broadcasterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class CancellableThread extends Thread {
        protected volatile boolean isCancelled = false;
        public void cancel() {
            isCancelled = true;
        }
    }
}
