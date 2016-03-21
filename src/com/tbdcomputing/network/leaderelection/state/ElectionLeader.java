package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageType;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageUtils;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dpho on 3/11/16.
 * <p>
 * Listens:
 * - any message with term > myterm
 *      -> convert to follower
 */
public class ElectionLeader extends ElectionState {
    private Timer timer; // timer has a task running to send out heartbeats at specific intervals
                            // reference is kept in order to stop the task if this server is no longer leader

    /**
     * Constructor for ElectionLeader. Start a new TimerTask that will send out heartbeats every HEARTBEAT_INTERVAL.
     *
     * @param e context which is passed from state to state
     */
    public ElectionLeader(ElectionStateContext e) {
        super(e);

        // Starts up the task to send out a heartbeat to all nodes in the cluster
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }, 0, ElectionSettings.HEARTBEAT_INTERVAL);
    }

    /**
     * Actions taken when a Leader receives a Heartbeat message from another node.
     * <p>
     * Check term. This means that there is another Leader in the cluster. If the other leader
     * has the same term, we revert to a Candidate and start a new vote!
     * <p>
     * Changes to CandidateState if equal terms or FollowerState if sender's term is higher
     *
     * @return resulting state after receiving this message
     */
    @Override
    public ElectionState handleHeartbeat(JSONObject message) {
        ElectionState result = this;

        long term = message.getLong("term");
        if (term == context.getTerm()) {
            result = transition(ElectionStateType.CANDIDATE);
        } else if (term > context.getTerm()) {
            context.setTerm(term);
            result = transition(ElectionStateType.FOLLOWER);
        }

        return result;
    }

    /**
     * Ignore the VoteGranted and treat it as a Response. Only state change occurs when your term is outdated.
     *
     * @return resulting state after receiving this message
     */
    @Override
    public ElectionState handleVoteGranted(JSONObject message) {
        return handleResponse(message);
    }

    /**
     * Broadcasts a suppression message to all nodes in the cluster through heartbeats
     */
    private void sendHeartbeat() {
        JSONObject msg = ElectionMessageUtils.makeMessage(context.getTerm(), context.getMyAddr(), ElectionMessageType.HEARTBEAT);
        context.getSender().broadcast(msg, context.getManager().getNodes());
    }

    /**
     * Leaders don't have a socket timeout
     *
     * @return 0 - this disables socket timeout
     */
    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public void close() {
        timer.cancel();
        context.setVoted(false);
    }
}
