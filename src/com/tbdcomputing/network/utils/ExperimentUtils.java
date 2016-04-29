package com.tbdcomputing.network.utils;

import com.tbdcomputing.network.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Utilities for running experiments
 */
public class ExperimentUtils {

    public static boolean PROXY_MODE = false;
    public static ArrayList<InetAddress> clusterAddresses;
    public static long electionStartTime;
    public static long electionStopTime;
    public static boolean electionStopTimeIsSet = false;
    public static final String ELECTION_LOG_FP = "./election.txt";

    /**
     * Reads in the proxy list of IP addresses for circumventing gossip and network discovery
     */
    public static void refreshClusterAddressList(){
        File file = new File("cluster_ips.txt");

        clusterAddresses = new ArrayList<InetAddress>();
        try {
            Scanner in = new Scanner(file);
            while(in.hasNextLine()){
                String IPAddressStr = in.nextLine();
                System.out.println(IPAddressStr);
                InetAddress address = InetAddress.getByName(IPAddressStr);
                if(!Constants.findLocalAddress().equals(address)){
                    clusterAddresses.add(address);
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        //TODO create gossip nodes from these public ips and instantiate Constants.cluster with them
    }
}
