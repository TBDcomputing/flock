package com.tbdcomputing.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Created by akatkov on 2/22/16.
 */
public class Constants {
    public static final int NETWORK_DISCOVERY_PORT = 8888;
    public static final String BROADCAST_ADDRESS = "255.255.255.255";
    public static final int GOSSIP_PORT = 8889;
    public static final int GOSSIP_RECEIVE_PORT = 8890;
    public static final long GOSSIP_DEATH_TIMER = 30000;
    public static final long GOSSIP_LEAVING_TIMER = 15000;
    public static final int ELECTION_PORT = 8891;
    public static final int ELECTION_RECEIVE_PORT = 8892;
    public static final int API_PORT = 8900;
    public static final int API_INTERNAL_PORT = 8901;

    public static String getUUID() {
        try {
            return searchForMac();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<String> PREFERRED_NETWORK_INTERFACES = Arrays.asList("wlan0", "en0", "en1", "en2", "eth0");
    public static String searchForMac() throws SocketException {
        Map<String, String> addressByNetwork =  new HashMap<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            byte[] bmac = network.getHardwareAddress();

            if (bmac != null && bmac.length > 0 ) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bmac.length; i++){
                    sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));
                }
                addressByNetwork.put(network.getName(), sb.toString());
//                System.out.println("Address = "+sb.toString()+" @ ["+network.getName()+"] "+network.getDisplayName());
            }
        }

        for (String name: addressByNetwork.keySet()) {
            if (PREFERRED_NETWORK_INTERFACES.contains(name)) {
                return addressByNetwork.get(name);
            }
        }

        return null;
    }

    public static InetAddress findLocalAddress() throws SocketException {
        Map<String, InetAddress> addressByNetwork =  new HashMap<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetInterfaces = network.getInetAddresses();
            while (inetInterfaces.hasMoreElements()) {
                InetAddress addr = inetInterfaces.nextElement();
                if (addr instanceof Inet4Address && !addr.isLinkLocalAddress() && !addr.isLoopbackAddress()) {
                    addressByNetwork.put(network.getName(), addr);
                }
            }
        }

        for (String name: addressByNetwork.keySet()) {
            if (PREFERRED_NETWORK_INTERFACES.contains(name)) {
                return addressByNetwork.get(name);
            }
        }

        return null;
    }
}
