package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageType;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageUtils;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 * Created by dpho on 3/11/16.
 *
 * Upon creation sends requestVotes out to every other server.
 *
 * Listens to other servers to record votes.
 * Messages can be:
 * - Heartbeat
 *      -> If the term is the same or greater, revert to a Follower.
 *      -> if the term is less, respond back to tell that server that it should become a Follower.
 * - Vote
 *      -> Record the vote and check if n/2 threshold has been reached.
 *          -> Promote to leader if so
 */
public class ElectionCandidate extends ElectionState {
    private int votes = 1; // initialized to 1 as we always vote for ourselves.

    public ElectionCandidate(ElectionStateContext e) {
        super(e);
        context.incrementTerm();
        context.setVoted(true);
        startElection();
    }

    /**
     * Actions taken when a server in LeaderState sends you a heartbeat.
     * <p>
     * Ignore if outdated term. Convert to follower if it isn't outdated.
     * If it's the same term, we have two leaders. Convert to a Candidate and start a new vote.
     *
     * @return resulting state after receiving this message
     */
    @Override
    public ElectionState handleHeartbeat(JSONObject message) {
        ElectionState result = this;

        long term = message.getLong("term");
        if (term >= context.getTerm()) {
            context.setTerm(term);
            try {
                context.setLeaderAddr(InetAddress.getByName(message.getString("sender")));
            } catch (UnknownHostException e) {
                log.log(Level.SEVERE, "Cannot find leader node's address. It has died, or there is a serious problem.");
            }
            result = transition(ElectionStateType.FOLLOWER);
        }

        return result;
    }

    /**
     * Actions taken when we receive a VoteGranted message in response to our VoteRequest
     * <p>
     * Increment our vote variable and check to see if we've met the majority threshold.
     *
     * @return resulting state after receiving this message
     */
    @Override
    public ElectionState handleVoteGranted(JSONObject message) {
        ElectionState result = this;

        long term = message.getLong("term");
        if (term > context.getTerm()) {
            context.setTerm(term);
            result = transition(ElectionStateType.FOLLOWER);
        } else if (term <= context.getTerm() && incrementVote()) {
            result = transition(ElectionStateType.LEADER);
        }

        return result;
    }

    /**
     * Start an election by sending a RequestVote to all nodes in the cluster.
     */
    private void startElection() {
        log.log(Level.INFO, "Starting an election now.");
        JSONObject msg = ElectionMessageUtils.makeMessage(context.getTerm(),
                context.getMyAddr(), ElectionMessageType.REQUESTVOTE);
        context.getSender().broadcast(msg, context.getManager().getNodes());
    }

    /**
     * Increment the vote and check to see if threshold has been reached
     *
     * @return should I be promoted?
     */
    private synchronized boolean incrementVote() {
        votes++;
        log.log(Level.INFO, "I have {0} votes and I need at least {1}.",
                new int[]{votes, ((context.getManager().getNodes().size() + 1) / 2) + 1});
        return votes >= ((context.getManager().getNodes().size() + 1) / 2) + 1;
    }

    /**
     * Randomized time for the socket to wait during an election
     *
     * @return socket timeout in milliseconds
     */
    @Override
    public int getTimeout() {
        return ElectionSettings.MINIMUM_TIMEOUT + (int) (Math.random() * ElectionSettings.ELECTION_TIMEOUT_SEED);
    }

    @Override
    public void close() {
        context.setVoted(false);
    }

}
