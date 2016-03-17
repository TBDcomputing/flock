package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;

/**
 * Created by dpho on 3/11/16.
 *
 * Mainly a listener.
 *
 * Receives messages from candidates and leaders:
 * - receives a RequestVote with term > myterm
 *      -> update term and vote for server that sent RequestVote
 * - receives a RequestVote with term <= myterm
 *      -> ignore and respond with your term
 * - heartbeat
 *      -> reset timer
 * - socket timeout?
 *      -> convert to candidate and increment term!
 */
public class ElectionFollower extends ElectionState {

    public ElectionFollower(ElectionStateContext e) {
        super(e);
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public ElectionState handleRequestVote() {
        return this;
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public ElectionState handleHeartbeat() {
        return this;
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public ElectionState handleVoteGranted() {
        return this;
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public ElectionState handleResponse() {
        return this;
    }

    /**
     * Randomized time for the socket to wait for a heartbeat from the leader
     *
     * @return socket timeout in milliseconds
     */
    @Override
    public int getTimeout() {
        return ElectionSettings.MINIMUM_TIMEOUT + (int) (Math.random() * ElectionSettings.HEARTBEAT_TIMEOUT_SEED);
    }
}
