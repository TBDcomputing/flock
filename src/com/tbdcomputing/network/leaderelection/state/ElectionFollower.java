package com.tbdcomputing.network.leaderelection.state;

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

    public void destroy() {

    }
}
