package com.tbdcomputing.network.leaderelection.bully.state;

import com.tbdcomputing.network.leaderelection.ElectionSettings;
import com.tbdcomputing.network.leaderelection.bully.BullyElectionSettings;
import com.tbdcomputing.network.leaderelection.bully.message.BullyElectionMessageType;
import com.tbdcomputing.network.leaderelection.bully.message.BullyElectionMessageUtils;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageType;
import com.tbdcomputing.network.leaderelection.message.ElectionMessageUtils;
import com.tbdcomputing.network.utils.ExperimentUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dpho on 3/11/16.
 * <p>
 * Listens:
 * - any message with term > myterm
 *      -> convert to follower
 */
public class BullyElectionLeader extends BullyElectionState {
    private Timer timer; // timer has a task running to send out supressions at specific intervals
    // reference is kept in order to stop the task if this server is no longer leader

    /**
     * Constructor for ElectionLeader. Start a new TimerTask that will send out heartbeats every HEARTBEAT_INTERVAL.
     *
     * @param e context which is passed from state to state
     */
    public BullyElectionLeader(BullyElectionStateContext e) {
        super(e);

        // Starts up the task to send out a heartbeat to all nodes in the cluster
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendSuppression();
            }
        }, 0, BullyElectionSettings.SITDOWN_INTERVAL);
    }

    @Override
    public BullyElectionState handleElection(JSONObject message) {
        sendSitdownMessage(message.getString("sender"));
        return this;
    }


    @Override
    public BullyElectionState handleSitdown(JSONObject message) {
        double alpha = message.getDouble("alpha");
        if (alpha <= this.context.getAlpha()) {
            sendSitdownMessage(message.getString("sender"));
            return this;
        } else {
            return transition(BullyElectionStateType.FOLLOWER);
        }
    }

    /**
     * Broadcasts a suppression message to all nodes in the cluster through heartbeats
     */
    private void sendSuppression() {
        JSONObject msg = BullyElectionMessageUtils.makeMessage(
                this.context.getAlpha(), context.getMyAddr(), BullyElectionMessageType.SITDOWN);
        context.getSender().broadcast(msg, context.getManager().getNodes());
        System.err.printf("sending suppression");
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
    }
}
