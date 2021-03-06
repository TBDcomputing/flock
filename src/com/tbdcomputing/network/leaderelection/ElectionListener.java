package com.tbdcomputing.network.leaderelection;

import com.tbdcomputing.network.Constants;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receives all messages
 * <p>
 * Created by dpho on 3/14/16.
 */
public abstract class ElectionListener extends Thread {
    final Logger log = Logger.getLogger(ElectionListener.class.getName());
    private DatagramSocket socket;

    public ElectionListener() {
        try {
            this.socket = new DatagramSocket(Constants.ELECTION_RECEIVE_PORT);
            this.socket.setReuseAddress(true);
        } catch (SocketException se) {
            log.log(Level.SEVERE, se.toString(), se);
        }
    }

    /**
     * Listening for any messages from other nodes.
     */
    public JSONObject listen() {
        JSONObject result = null;

        try {
            // grabbing the JSONObject message from other server
            byte[] buf = new byte[10240];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            result = new JSONObject(new String(packet.getData(), 0, packet.getLength()));
        } catch (SocketTimeoutException ste) {
            // timed out! become a candidate and start a vote!
            log.log(Level.INFO, "Socket timed out! Reverting to CandidateState.");
            onTimeout();
        } catch (SocketException e) {
            // Expected to be thrown when the "quit" command is given.
            // log.log(Level.SEVERE, e.toString(), e);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, ioe.toString(), ioe);
        }

        return result;
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
            // log.log(Level.INFO, "Timeout is now set to {0} ms.", timeout);
        } catch (SocketException se) {
            log.log(Level.SEVERE, se.toString(), se);
        }
    }

    /**
     * Become a candidate!
     * You were either a follower and your heartbeat timed out => you become a candidate
     * You were a candidate and did not receive majority votes,
     * but no leader was declared => become a candidate again
     */
    public abstract void onTimeout();

    public void close() {
        socket.close();
    }
}
