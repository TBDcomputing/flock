package com.tbdcomputing.network.discovery;

import com.tbdcomputing.network.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class broadcasts to every other node on the network and then waits for a
 * response. Any node that responds will be running the listener. We then gather
 * node data for running gossip protocol.
 * 
 * @author drew
 *
 */
public class NetworkDiscoveryBroadcaster implements Runnable {

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
			sock.setSoTimeout(10000);
			byte[] buf = new byte[1000];

			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter a message: ");
			byte[] msg = scanner.nextLine().getBytes();
			System.arraycopy(msg, 0, buf, 0, msg.length);

			DatagramPacket data = new DatagramPacket(buf, buf.length, addr, Constants.PORT);
			sock.send(data);

			while (!sock.isClosed()) {
				try {
					DatagramPacket recv = new DatagramPacket(buf, buf.length);

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

	@Override
	public void run() {
		try {
			InetAddress addr = InetAddress.getByName(Constants.BROADCAST_ADDRESS);

			DatagramSocket sock = new DatagramSocket();
			sock.setBroadcast(true);
			sock.setSoTimeout(1000);
			byte[] buf = new byte[1000];

			DatagramPacket data = new DatagramPacket(buf, buf.length, addr, Constants.PORT);
			sock.send(data);
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
