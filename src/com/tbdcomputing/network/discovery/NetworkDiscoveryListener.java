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
					System.out.println("Recieved a packet! Data: " + new String(packet.getData()));

					// Don't respond to ourself because we will fail to bind to
					// our same port
					System.out.println(InetAddress.getLocalHost() + " | " + packet.getAddress() + " | " + InetAddress.getLocalHost().equals(packet.getAddress()));

					if (!InetAddress.getLocalHost().equals(packet.getAddress())) {
					
						socket.send(new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SocketException e1) {
			System.out.println("Failed to create socket.");
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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
