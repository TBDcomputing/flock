package com.tbdcomputing.network.discovery;

import java.net.*;

/**
 * This class listens for UDP broadcasts from any clients interested in gathering this node's information. It responds
 * with the listener's IP address and any other relevant information for the purpose of initiating gossip on the
 * broadcasting node.
 */
public class NetworkDiscoveryListener implements Runnable {

	/**
	 * Listens for a broadcast, and then responds with the listener's IP address and any other relevant gossip
	 * information.
	 */
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
					System.out.println(InetAddress.getLocalHost() + " | " + packet.getAddress() + " | " + InetAddress.getLocalHost().equals(packet.getAddress())); //useful testing code

					if (!InetAddress.getLocalHost().equals(packet.getAddress())) { //TODO comparison by UUID
					
						socket.send(new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort()));//TODO send back gossip data
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
