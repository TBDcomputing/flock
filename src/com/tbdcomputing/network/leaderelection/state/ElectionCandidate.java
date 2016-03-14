package com.tbdcomputing.network.leaderelection.state;

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
    private int votes = 0;

    public ElectionCandidate(ElectionStateContext e) {
        super(e);
        e.incrementTerm();
        startElection();
    }

    private void startElection() {
        // send requestVote message
    }

    public synchronized void incrementVote() {
        votes++;
        /* Check if votes >= n/2 where n := size(cluster) */
    }

    public void destroy() {

    }
}
