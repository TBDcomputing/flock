package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;

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
        e.incrementTerm();
        startElection();
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
        return transition(ElectionStateType.FOLLOWER);
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public ElectionState handleVoteGranted() {
        return incrementVote() ? transition(ElectionStateType.LEADER) : this;
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
     * Start an election by sending a RequestVote to all nodes in the cluster.
     */
    private void startElection() {

    }

    /**
     * Increment the vote and check the threshold
     * TODO
     *
     * @return has threshold been reached?
     */
    private synchronized boolean incrementVote() {
        votes++;
        return true;
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
}
