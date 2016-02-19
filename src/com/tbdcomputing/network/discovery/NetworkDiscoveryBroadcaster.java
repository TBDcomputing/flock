package com.tbdcomputing.network.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class broadcasts to every other node on the network and then waits for a
 * response. Any node that responds will be running the listener. We then gather
 * node data for running gossip protocol.
 * 
 * @author drew
 *
 */
public class NetworkDiscoveryBroadcaster {
	private static final int PORT = 8888;

	/**
	 * Currently does nothing.
	 */
	public NetworkDiscoveryBroadcaster() {
		// Intentionally left blank
	}

	/**
	 * Creates a list of IP addresses based on nodes that respond to the
	 * broadcast. Any node that responds will be added to the list.
	 * 
	 * @return List of IP addresses that responded to the broadcast. Returns
	 *         null if failed to complete broadcast.
	 */
	public List<String> findHosts() {
		InetAddress addr;
		ArrayList<String> addresses = new ArrayList<String>();

		try {
			addr = InetAddress.getByName("255.255.255.255");

			DatagramSocket sock = new DatagramSocket();
			sock.setBroadcast(true);
			sock.setSoTimeout(3000);
			byte[] buf = new byte[1000];

			DatagramPacket data = new DatagramPacket(buf, buf.length, addr, PORT);
			sock.send(data);

			DatagramPacket recv = new DatagramPacket(buf, 0, buf.length);
			while (!sock.isClosed()) {
				try {
					sock.receive(recv);
					String rcvd = "rcvd from " + recv.getAddress() + ", " + recv.getPort() + ": "
							+ new String(recv.getData(), 0, recv.getLength());
					System.out.println(rcvd);
					addresses.add(rcvd);

				} catch (SocketTimeoutException e) {
					// timeout exception.
					System.out.println("Timeout reached!!! " + e);
					sock.close();
				}
			}
		} catch (UnknownHostException e1) {
			System.out.println("Failed to set broadcast address on 255.255.255.255");
			e1.printStackTrace();
			return null;
		} catch (SocketException e1) {
			System.out.println("Failed to create socket to broadcast");
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			System.out.println("Failed to open receiving socket");
			e1.printStackTrace();
			return null;
		}

		return addresses;
	}

	public static void main(String[] args) throws IOException {
		NetworkDiscoveryBroadcaster caster = new NetworkDiscoveryBroadcaster();
		caster.findHosts();
	}

}
