package com.tbdcomputing.network.leaderelection;

import com.tbdcomputing.network.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Receives all messages
 *
 * Created by dpho on 3/14/16.
 */
public abstract class ElectionListener {
    private DatagramSocket socket;
    private long lastHeartbeat;

    public ElectionListener() {
        this.lastHeartbeat = System.currentTimeMillis();

        try {
            this.socket = new DatagramSocket(Constants.ELECTION_RECEIVE_PORT);
            this.socket.setReuseAddress(true);
        } catch (SocketException se) {
            se.printStackTrace();
        }
    }

    /**
     * Listening for any messages from other nodes.
     */
    public void listen() {
        lastHeartbeat = System.currentTimeMillis();

        try {
            //TODO: set max size for buf in Constants, and use it here
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
        } catch (SocketTimeoutException ste) {
            // timed out! become a candidate and start a vote!
            onTimeout();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * @return the time left until our heartbeat timer expires and we transition into CandidateState
     */
    public int timeSinceLastHeartbeat() {
        return (int) (System.currentTimeMillis() - lastHeartbeat);
    }

    /**
     * Set a new timeout for the socket. Unfortunately, we have to interrupt the socket and
     * create a new one to accomplish this.
     *
     * @param timeout new timeout period
     */
    public synchronized void setSocketTimeout(int timeout) {
        try {
            socket.close();
            this.socket = new DatagramSocket(Constants.ELECTION_RECEIVE_PORT);
            this.socket.setReuseAddress(true);
            this.socket.setSoTimeout(timeout);
        } catch (SocketException se) {
            se.printStackTrace();
        }
    }

    /**
     * Become a candidate!
     * You were either a follower and your heartbeat timed out => you become a candidate
     * You were a candidate and did not receive majority votes,
     * but no leader was declared => become a candidate again
     */
    public abstract void onTimeout();
}
