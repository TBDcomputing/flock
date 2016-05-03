package com.tbdcomputing.network.leaderelection.bully;

import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.leaderelection.ElectionListener;
import com.tbdcomputing.network.leaderelection.ElectionSender;
import com.tbdcomputing.network.leaderelection.bully.state.*;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single-threaded election manager. Will listen for messages from other servers and process them sequentially.
 * <p>
 * Created by dpho on 3/12/16.
 */
public class BullyElectionManager extends Thread {
    private final Logger log = Logger.getLogger(BullyElectionManager.class.getName());
    private GossipManager manager;
    private BullyElectionState state;
    private final ElectionListener listener = new ElectionListener() {
        @Override
        public void onTimeout() {
            if (state instanceof BullyElectionCandidate) {
                state = state.transition(BullyElectionStateType.LEADER);
            } else {
                state = state.transition(BullyElectionStateType.CANDIDATE);
            }
        }
    };

    public BullyElectionManager(GossipManager manager) {
        this.manager = manager;

    }

    @Override
    public void run() {
        state = new BullyElectionFollower(new BullyElectionStateContext(manager, new ElectionSender()));
        listener.setSocketTimeout(0); // start with unlimited timeout
        while (!Thread.interrupted()) {
            JSONObject message;
            if ((message = listener.listen()) != null) {
                log.log(Level.INFO, "Received a {0} message from {1}.",
                        new String[]{message.getString("type"), message.getString("sender")});
                state = state.handleMessage(message);
            }

            // new timeout is dependent on current state
            listener.setSocketTimeout(state.getTimeout());
        }
    }

    public void startElection() {
        listener.setSocketTimeout(1);
    }

    public void startElection(String configStr) {
        this.manager.getMe().refreshAlphaValue(configStr);
        //TODO if one node sends out a config at 5 minutes and another at 10 minutes, which config to go with?
        listener.setSocketTimeout(1);
    }

    public BullyElectionState getElectionState() {
        return state;
    }

    /**
     * Closes the socket on system shutdown
     */
    @Override
    public void interrupt() {
        listener.close();
        state.close();
        super.interrupt();
    }
}
