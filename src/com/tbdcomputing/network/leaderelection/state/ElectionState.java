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
    protected ElectionStateContext context;

    public ElectionState(ElectionStateContext e) {
        context = e;
    }

    public long getTerm() {
        return context.getTerm();
    }

    /**
     * Methods to handle messages from other nodes.
     */
    public abstract ElectionState handleRequestVote();

    public abstract ElectionState handleHeartbeat();

    public abstract ElectionState handleVoteGranted();

    public abstract ElectionState handleResponse();

    /**
     * @return timeout in milliseconds (0 implies no timeout)
     */
    public abstract int getTimeout();

    /**
     * Transitions into another state.
     *
     * @param type the state to transition to
     * @return new state
     */
    public ElectionState transition(ElectionStateType type) {
        switch (type) {
            case LEADER:
                return new ElectionLeader(this.context);
            case CANDIDATE:
                return new ElectionCandidate(this.context);
            case FOLLOWER:
                return new ElectionFollower(this.context);
            default:
                return null;
        }
    }

}
