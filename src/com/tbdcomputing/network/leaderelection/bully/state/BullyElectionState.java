package com.tbdcomputing.network.leaderelection.bully.state;

import com.tbdcomputing.network.leaderelection.bully.message.BullyElectionMessageType;
import com.tbdcomputing.network.leaderelection.bully.message.BullyElectionMessageUtils;
import com.tbdcomputing.network.utils.ExperimentUtils;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standard ElectionState class which Followers, Candidates, and Leaders extend.
 */
public abstract class BullyElectionState {
    protected final Logger log = Logger.getLogger(BullyElectionState.class.getName());
    protected BullyElectionStateContext context;

    public BullyElectionState(BullyElectionStateContext e) {
        context = e;
    }

    public abstract BullyElectionState handleElection(JSONObject message);


    public abstract BullyElectionState handleSitdown(JSONObject message);

    /**
     * @return timeout in milliseconds (0 implies no timeout)
     */
    public abstract int getTimeout();

    /**
     * Clean up the state prior to transitioning.
     */
    public abstract void close();


    public BullyElectionState handleMessage(JSONObject message) {
        BullyElectionMessageType type = BullyElectionMessageType.valueOf(message.getString("type"));
        switch (type) {
            case ELECTION:
                return this.handleElection(message);
            case SITDOWN:
                return this.handleSitdown(message);
            default:
                return this;
        }
    }

    public void sendSitdownMessage(String sendTo) {
        try {
            JSONObject msg = BullyElectionMessageUtils.makeMessage(context.getAlpha(),
                    context.getMyAddr(), BullyElectionMessageType.SITDOWN);
            InetAddress voteDst = InetAddress.getByName(sendTo);
            context.getSender().sendMessage(msg, voteDst);
        } catch (UnknownHostException e) {
            //TODO: Do something
        }
    }
    /**
     * Transitions into another state.
     *
     * @param type the state to transition to
     * @return new state
     */
    public BullyElectionState transition(BullyElectionStateType type) {
        // clean up loose ends before transitioning
        this.close();

        log.log(Level.INFO, String.format("Transitioning into %s.", type.name()));
        switch (type) {
            case LEADER:
                if(!ExperimentUtils.electionStopTimeIsSet){
                    ExperimentUtils.electionStopTime = System.currentTimeMillis();
                    ExperimentUtils.electionStopTimeIsSet = true;

                    try {
                        File file = new File(ExperimentUtils.ELECTION_LOG_FP);

                        if (!file.exists()) {
                            file.createNewFile();
                        }
//                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
//
//                        BufferedWriter bw = new BufferedWriter(fw);
//
//                        bw.write("Election commenced at: "+ ExperimentUtils.electionStartTime);
//                        System.out.println("Election commenced at: "+ ExperimentUtils.electionStartTime);
//
//                        bw.close();


                        Files.write(Paths.get(ExperimentUtils.ELECTION_LOG_FP ), ("\nleader elected at: " + ExperimentUtils.electionStopTime).getBytes(), StandardOpenOption.APPEND);
                    }catch (IOException e) {

                    }
                    System.out.println("leader elected at: " + ExperimentUtils.electionStopTime);
                }
                return new BullyElectionLeader(this.context);
            case CANDIDATE:
                return new BullyElectionCandidate(this.context);
            case FOLLOWER:
                return new BullyElectionFollower(this.context);
            default:
                return null;
        }
    }

}
