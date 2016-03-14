package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.leaderelection.ElectionSettings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Followers:
 * - Respond to messages sent by Candidates and Leaders
 * <p>
 * Candidates:
 * - Start a RequestVote when newly promoted
 * - Tally up votes
 * - When votes >= N/2, become a leader
 * <p>
 * Leaders:
 * - Send out a heartbeat every ~150ms in order to suppress Followers
 */
public abstract class ElectionState {
    private ElectionStateContext context;
    private DatagramSocket socket;

    public ElectionState(ElectionStateContext e) {
        context = e;
        try {
            this.socket = new DatagramSocket(Constants.ELECTION_RECEIVE_PORT);
            this.socket.setReuseAddress(true);
            this.socket.setSoTimeout(ElectionSettings.ELECTION_TIMEOUT_SEED);
        } catch (SocketException se) {
            se.printStackTrace();
        }
    }

    public void listen() {
        try {
            //TODO: set max size for buf in Constants, and use it here
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
        } catch (SocketTimeoutException ste) {
            // change to candidate, start vote!
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public abstract void destroy();
}
