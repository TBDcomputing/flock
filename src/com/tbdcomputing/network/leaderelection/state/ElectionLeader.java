package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dpho on 3/11/16.
 *
 * Listens:
 * - any message with term > myterm
 *      -> convert to follower
 *
 */
public class ElectionLeader extends ElectionState {
    private Timer timer; // timer has a task running to send out heartbeats at specific intervals
                            // reference is kept in order to stop the task if this server is no longer leader

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
        return this;
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public ElectionState handleVoteGranted() {
        return this;
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
     * Broadcasts a suppression message to all nodes in the cluster through heartbeats
     * TODO
     */
    private void sendHeartbeat() {
    }

    /**
     * Leaders don't have a socket timeout
     *
     * @return 0 which disables socket timeout
     */
    @Override
    public int getTimeout() {
        return 0;
    }

    /**
     * Cleans up leader before transitioning
     * TODO
     */
    private void close() {
        timer.cancel();
    }
}
