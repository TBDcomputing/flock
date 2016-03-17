package com.tbdcomputing.network.leaderelection.state;

/**
 * Created by dpho on 3/12/16.
 * <p>
 * Information that this server will keep between each state.
 */
public class ElectionStateContext {
    private long term = 0;
    private boolean voted = false;

    public ElectionStateContext() {

    }

    public synchronized void incrementTerm() {
        setTerm(getTerm() + 1);
    }

    public synchronized long getTerm() {
        return term;
    }

    public synchronized void setTerm(long term) {
        this.term = term;
    }

    public synchronized boolean isVoted() {
        return voted;
    }

    public synchronized void setVoted(boolean voted) {
        this.voted = voted;
    }
}
