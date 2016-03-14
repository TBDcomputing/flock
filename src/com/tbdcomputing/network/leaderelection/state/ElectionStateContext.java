package com.tbdcomputing.network.leaderelection.state;

/**
 * Created by dpho on 3/12/16.
 *
 * Information that this server will keep between each state.
 */
public class ElectionStateContext {
    private long term = 0;
    private boolean voted = false;

    public ElectionStateContext() {

    }

    synchronized void updateTerm(long newTerm) {
        term = newTerm;
    }

    synchronized void incrementTerm() {
        term++;
    }

    synchronized void reset() {
        voted = false;
    }
}
