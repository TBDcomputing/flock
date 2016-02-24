package com.tbdcomputing.network.discovery;

import com.tbdcomputing.network.Constants;
import org.json.JSONObject;
import org.omg.SendingContext.RunTime;
import sun.security.provider.MD5;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class broadcasts to every other node on the network. Any node that responds will be running the listener. The
 * listening node will decide its response behavior according to the behavior implemented in its
 * NetworkDiscoveryListener.
 *
 * @author drew
 */
public class NetworkDiscoveryBroadcaster implements Runnable {

    /**
     * Creates a list of IP addresses based on nodes that respond to the
     * broadcast. Any node that responds will be added to the list.
     *
     * @return List of IP addresses that responded to the broadcast. Returns
     *         null if failed to complete broadcast.
     */
//	public List<String> findHosts() {
//		InetAddress addr;
//		ArrayList<String> addresses = new ArrayList<String>();
//
//		try {
//			addr = InetAddress.getByName("255.255.255.255");
//
//			DatagramSocket sock = new DatagramSocket();
//			sock.setBroadcast(true);
//			sock.setSoTimeout(10000);
//			byte[] buf = new byte[1000];
//
//			Scanner scanner = new Scanner(System.in);
//			System.out.print("Enter a message: ");
//			byte[] msg = scanner.nextLine().getBytes();
//			System.arraycopy(msg, 0, buf, 0, msg.length);
//
//			DatagramPacket data = new DatagramPacket(buf, buf.length, addr, Constants.PORT);
//			sock.send(data);
//
//			while (!sock.isClosed()) {
//				try {
//					DatagramPacket recv = new DatagramPacket(buf, buf.length);
//
//					sock.receive(recv);
//					String rcvd = "rcvd from " + recv.getAddress() + ", " + recv.getPort() + ": "
//							+ new String(recv.getData(), 0, recv.getLength());
//					System.out.println(rcvd);
//					addresses.add(rcvd);
//
//				} catch (SocketTimeoutException e) {
//					// timeout exception.
//					System.out.println("Timeout reached!!! " + e);
//					sock.close();
//				}
//			}
//		} catch (UnknownHostException e1) {
//			System.out.println("Failed to set broadcast address on 255.255.255.255");
//			e1.printStackTrace();
//			return null;
//		} catch (SocketException e1) {
//			System.out.println("Failed to create socket to broadcast");
//			e1.printStackTrace();
//			return null;
//		} catch (IOException e1) {
//			System.out.println("Failed to open receiving socket");
//			e1.printStackTrace();
//			return null;
//		}
//
//		return addresses;
//	}

    /**
     * Broadcasts its UUID to all the nodes on the network.
     */
    @Override
    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(Constants.BROADCAST_ADDRESS);

            // don't need to use our port for broadcasting our node's information
            DatagramSocket sock = new DatagramSocket();
            sock.setBroadcast(true);
            sock.setSoTimeout(1000);

            JSONObject json = new JSONObject();
            String uuid = Constants.getUUID();
            if (uuid == null) {
                // TODO: better behavior for when no UUID can be generated
                // probably means no mac address meaning no internet
                throw new RuntimeException("Couldn't generate UUID");
            }
            json.put("id", uuid);
            // TODO: add more data about this node to the JSONObject

            byte[] buf = json.toString().getBytes();
            // set the destination to be our predetermined port
            DatagramPacket data = new DatagramPacket(buf, buf.length, addr, Constants.NETWORK_DISCOVERY_PORT);
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
