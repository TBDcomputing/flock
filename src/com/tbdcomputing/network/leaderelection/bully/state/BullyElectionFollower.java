package com.tbdcomputing.network.leaderelection.bully.state;

import com.tbdcomputing.network.leaderelection.bully.BullyElectionSettings;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
public class BullyElectionFollower extends BullyElectionState {

    public BullyElectionFollower(BullyElectionStateContext e) {
        super(e);
    }

    @Override
    public BullyElectionState handleElection(JSONObject message) {
        // TODO: Change to Double
        double alpha = message.getDouble("alpha");
        System.err.printf("Comparing alpha: %s to my alpha: %s\n", alpha, this.context.getAlpha());
        if (alpha >= this.context.getAlpha()) {
            return this;
        } else {
            sendSitdownMessage(message.getString("sender"));
            return transition(BullyElectionStateType.CANDIDATE);
        }
    }

    @Override
    public BullyElectionState handleSitdown(JSONObject message) {
        double alpha = message.getDouble("alpha");
        if (alpha <= this.context.getAlpha()) {
            sendSitdownMessage(message.getString("sender"));
            return transition(BullyElectionStateType.CANDIDATE);
        } else {
            try {
                context.setLeaderAddr(InetAddress.getByName(message.getString("sender")));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return this;
        }
    }

    /**
     * Randomized time for the socket to wait for a heartbeat from the leader
     *
     * @return socket timeout in milliseconds
     */
    @Override
    public int getTimeout() {
        return BullyElectionSettings.FOLLOWER_TIMEOUT;
    }

    @Override
    public void close() {}
}
