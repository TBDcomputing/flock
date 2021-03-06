package com.tbdcomputing.network.discovery;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.gossip.GossipNode;
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
    private GossipNode me;

    public NetworkDiscoveryBroadcaster(GossipNode me) {
        this.me = me;
    }

    /**
     * Broadcasts its UUID to all the nodes on the network.
     */
    @Override
    public void run() {
        DatagramSocket sock = null;
        try {
            InetAddress addr = InetAddress.getByName(Constants.BROADCAST_ADDRESS);

            // don't need to use our port for broadcasting our node's information
            sock = new DatagramSocket();
            sock.setBroadcast(true);
            sock.setSoTimeout(1000);

            JSONObject json = me.toJSON();
            byte[] buf = json.toString().getBytes();
            // set the destination to be our predetermined port
            DatagramPacket data = new DatagramPacket(buf, buf.length, addr, Constants.NETWORK_DISCOVERY_PORT);
            sock.send(data);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sock != null) {
                sock.close();
            }
        }
    }
}
