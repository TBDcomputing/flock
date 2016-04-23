package com.tbdcomputing.network.leaderelection.bully.state;

import com.tbdcomputing.network.leaderelection.bully.BullyElectionSettings;
import com.tbdcomputing.network.leaderelection.bully.message.BullyElectionMessageType;
import com.tbdcomputing.network.leaderelection.bully.message.BullyElectionMessageUtils;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.logging.Level;

/**
 * Created by dpho on 3/11/16.
 *
 * Upon creation sends requestVotes out to every other server.
 *
 * Listens to other servers to record votes.
 * Messages can be:
 * - Election
 *      -> If they have higher alpha become a follower
 *      -> If you have higher alpha tell them to sitdown
 * - Sitdown
 *      -> You have been told to sit down by someone with a higher alpha, proceed to become a Follower
 */
public class BullyElectionCandidate extends BullyElectionState {
    public BullyElectionCandidate(BullyElectionStateContext e) {
        super(e);
        startElection();
    }

    @Override
    public BullyElectionState handleElection(JSONObject message) {
        // TODO: Change to Double
        double alpha = message.getDouble("alpha");

        System.err.printf("Comparing alpha: %s to my alpha: %s\n", alpha, this.context.getAlpha());
        if (alpha >= this.context.getAlpha()) {
            return transition(BullyElectionStateType.FOLLOWER);
        } else {
            sendSitdownMessage(message.getString("sender"));
            return this;
        }
    }

    /**
     * Start an election by sending a Election message to all nodes in the cluster.
     */
    private void startElection() {
        log.log(Level.INFO, "Starting an election now.");
        JSONObject msg = BullyElectionMessageUtils.makeMessage(context.getAlpha(),
                context.getMyAddr(), BullyElectionMessageType.ELECTION);
        context.getSender().broadcast(msg, context.getManager().getNodes());
    }

    /**
     * Randomized time for the socket to wait during an election
     *
     * @return socket timeout in milliseconds
     */
    @Override
    public int getTimeout() {
        return BullyElectionSettings.CANDIDATE_TIMEOUT;
    }

    @Override
    public void close() {}
}
