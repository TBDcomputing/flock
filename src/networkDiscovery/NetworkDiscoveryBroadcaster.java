package networkDiscovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class NetworkDiscoveryBroadcaster {
	private static final int PORT = 8888;
	public NetworkDiscoveryBroadcaster() {
		// Intentionally left blank
	}
	
	public List<String> findHosts() throws IOException {
		InetAddress addr = InetAddress.getByName("255.255.255.255");
		DatagramSocket sock = new DatagramSocket();
		sock.setBroadcast(true);
		sock.setSoTimeout(3000);
		byte[] buf = new byte[1000];

		
		ArrayList<String> addresses = new ArrayList<String>();
		
		DatagramPacket data = new DatagramPacket(buf, buf.length, addr, PORT);
		sock.send(data);
		
		DatagramPacket recv = new DatagramPacket(buf, 0, buf.length);
		while(!sock.isClosed()) {
			try {
                sock.receive(recv);
                String rcvd = "rcvd from " + recv.getAddress() + ", " + recv.getPort() + ": "+ new String(recv.getData(), 0, recv.getLength());
                System.out.println(rcvd);
                addresses.add(rcvd);
                
            }
            catch (SocketTimeoutException e) {
                // timeout exception.
                System.out.println("Timeout reached!!! " + e);
                sock.close();
            }
		}
		
		return addresses;
	}
	
	public static void main(String[] args) throws IOException {
		NetworkDiscoveryBroadcaster caster = new NetworkDiscoveryBroadcaster();
		caster.findHosts();
	}
	

}
