package com.tbdcomputing.network.discovery;

import java.net.*;

public class NetworkDiscoveryListener implements Runnable {
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(8888);
            while (true) {
                try {
                    byte[] buf = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    System.out.println("Recieved a packet!");
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (SocketException e1) {
            System.out.println("Failed to create socket.");
            e1.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                new NetworkDiscoveryListener().run();
            }
        }.start();
    }
}
