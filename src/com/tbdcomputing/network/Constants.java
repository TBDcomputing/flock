package com.tbdcomputing.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by akatkov on 2/22/16.
 */
public class Constants {
    public static final int NETWORK_DISCOVERY_PORT = 8888;
    public static final String BROADCAST_ADDRESS = "255.255.255.255";
    public static final int GOSSIP_PORT = 8889;
    public static final int GOSSIP_RECEIVE_PORT = 8890;

    public static String getUUID() {
        try {
            System.out.println("MAC Search: " + searchForMac());
            InetAddress address = InetAddress.getLocalHost();

            /*
             * Get NetworkInterface for the current host and then read
             * the hardware address.
             */
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    /*
                     * Extract each array of mac address and convert it
                     * to hexa with the following format
                     * 08-00-27-DC-4A-9E.
                     */
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    System.out.println("UUID: " + sb.toString());
                    return sb.toString();
                } else {
                    System.out.println("Address doesn't exist or is not " +
                            "accessible.");
                }
            } else {
                System.out.println("Network Interface for the specified " +
                        "address is not found.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String searchForMac() throws SocketException {
        String firstInterface = null;
        Map<String, String> addressByNetwork =  new HashMap<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();

            byte[] bmac = network.getHardwareAddress();
            if (bmac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bmac.length; i++){
                    sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));
                }

                if (!sb.toString().isEmpty()) {
                    addressByNetwork.put(network.getName(), sb.toString());
                    System.out.println("Address = "+sb.toString()+" @ ["+network.getName()+"] "+network.getDisplayName());
                }

                if (!sb.toString().isEmpty() && firstInterface == null) {
                    firstInterface = network.getName();
                }
            }
        }

        if (firstInterface != null) {
            return addressByNetwork.get(firstInterface);
        }

        return null;
    }
}
