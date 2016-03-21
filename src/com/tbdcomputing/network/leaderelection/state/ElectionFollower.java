package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;
import org.json.JSONObject;

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
     * Actions taken when a Leader receives a Heartbeat message from another node.
     * <p>
     * Update term if possible. No action needed.
     *
     * @return resulting state after receiving this message
     */
    @Override
    public ElectionState handleHeartbeat(JSONObject message) {
        return handleResponse(message);
    }

    /**
     * A Follower receives a Heartbeat message from another node.
     * <p>
     * Update term if ours is outdated. Take no action.
     *
     * @return resulting state after receiving this message
     */
    @Override
    public ElectionState handleVoteGranted(JSONObject message) {
        return handleResponse(message);
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

    @Override
    public void close() {
        context.setVoted(false);
    }
}
