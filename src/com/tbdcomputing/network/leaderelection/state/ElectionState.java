package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.message.ElectionMessageType;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageUtils;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standard ElectionState class which Followers, Candidates, and Leaders extend.
 */
public abstract class ElectionState {
    protected final Logger log = Logger.getLogger(ElectionState.class.getName());
    protected ElectionStateContext context;

    public ElectionState(ElectionStateContext e) {
        context = e;
    }

    public abstract ElectionState handleHeartbeat(JSONObject message);

    public abstract ElectionState handleVoteGranted(JSONObject message);

    /**
     * Actions taken when a Leader receives a RequestVote message.
     * <p>
     * If the term of the server asking for the vote is greater than ours, revert
     * to FollowerState and vote for him. If not, ignore it.
     *
     * @return resulting state after receiving this message
     */
    public ElectionState handleRequestVote(JSONObject message) {
        ElectionState result = this;

        long term = message.getLong("term");
        JSONObject msg = ElectionMessageUtils.makeMessage(context.getTerm(),
                context.getMyAddr(), ElectionMessageType.VOTEGRANTED);
        if (term > context.getTerm()) {
            context.setTerm(term);
            result = transition(ElectionStateType.FOLLOWER);
            sendVote(msg, message.getString("sender"));
        } else if (term == context.getTerm() && !context.getVoted()) {
            sendVote(msg, message.getString("sender"));
        }

        return result;
    }

    /**
     * All States respond to a Response the same way. Check the term and see if ours is outdated.
     *
     * @param message JSON message that we received from another node
     * @return resulting state after receiving this message
     */
    public ElectionState handleResponse(JSONObject message) {
        ElectionState result = this;

        long term = message.getLong("term");
        if (term > context.getTerm()) {
            context.setTerm(term);

            if ("heartbeat".equals(message.getString("type"))) {
                try {
                    context.setLeaderAddr(InetAddress.getByName(message.getString("sender")));
                } catch (UnknownHostException e) {
                    log.log(Level.SEVERE, "Cannot find leader node's address. It has died, or there is a serious problem.");
                }
            }
            result = transition(ElectionStateType.FOLLOWER);
        }

        return result;
    }

    /**
     * Send a VoteGranted message.
     *
     * @param sender String representation of the InetAddress we want to vote for
     */
    protected void sendVote(JSONObject msg, String sender) {
        try {
            log.log(Level.INFO, "SENDING VOTE TO " + sender);
            // Extract addr from the message to send your vote to
            InetAddress voteDst = InetAddress.getByName(sender);

            context.getSender().sendMessage(msg, voteDst);
            context.setVoted(true);
        } catch (UnknownHostException e) {
            // can't find the node? Don't vote for it.
            context.setVoted(false);
        }
    }

    /**
     * @return timeout in milliseconds (0 implies no timeout)
     */
    public abstract int getTimeout();

    /**
     * Clean up the state prior to transitioning.
     */
    public abstract void close();

    /**
     * Transitions into another state.
     *
     * @param type the state to transition to
     * @return new state
     */
    public ElectionState transition(ElectionStateType type) {
        // clean up loose ends before transitioning
        this.close();

        switch (type) {
            case LEADER:
                log.log(Level.INFO, "Transitioning into LeaderState at term #{0}.", context.getTerm());
                return new ElectionLeader(this.context);
            case CANDIDATE:
                log.log(Level.INFO, "Transitioning into CandidateState at term #{0}.", context.getTerm());
                return new ElectionCandidate(this.context);
            case FOLLOWER:
                log.log(Level.INFO, "Transitioning into FollowerState at term #{0}.", context.getTerm());
                return new ElectionFollower(this.context);
            default:
                return null;
        }
    }

}
