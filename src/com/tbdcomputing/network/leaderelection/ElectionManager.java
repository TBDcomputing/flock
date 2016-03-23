package com.tbdcomputing.network.leaderelection;

import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.leaderelection.state.ElectionFollower;
import com.tbdcomputing.network.leaderelection.state.ElectionState;
import com.tbdcomputing.network.leaderelection.state.ElectionStateContext;
import com.tbdcomputing.network.leaderelection.state.ElectionStateType;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single-threaded election manager. Will listen for messages from other servers and process them sequentially.
 * <p>
 * Created by dpho on 3/12/16.
 */
public class ElectionManager extends Thread {
    private final Logger log = Logger.getLogger(ElectionManager.class.getName());
    private GossipManager manager;
    private ElectionState state;
    private final ElectionListener listener = new ElectionListener() {
        @Override
        public void onTimeout() {
            state = state.transition(ElectionStateType.CANDIDATE);
        }
    };

    public ElectionManager(GossipManager manager) {
        this.manager = manager;
    }

    public void run() {
        state = new ElectionFollower(new ElectionStateContext(manager, new ElectionSender()));
        listener.setSocketTimeout(state.getTimeout());
        while (!Thread.interrupted()) {
            JSONObject message;
            if ((message = listener.listen()) != null) {

                log.log(Level.INFO, "Received a {0} message from {1}.",
                        new String[]{message.getString("type"), message.getString("sender")});

                switch (message.getString("type")) {
                    case "requestvote":
                        state = state.handleRequestVote(message);
                        break;
                    case "heartbeat":
                        state = state.handleHeartbeat(message);
                        break;
                    case "vote":
                        state = state.handleVoteGranted(message);
                        break;
                    case "response":
                        state = state.handleResponse(message);
                        break;
                }
            }

            // new timeout is dependent on current state
            listener.setSocketTimeout(state.getTimeout());
        }
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
