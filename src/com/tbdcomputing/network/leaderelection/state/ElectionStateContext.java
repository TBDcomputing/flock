package com.tbdcomputing.network.leaderelection.state;

import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.leaderelection.ElectionSender;

import java.net.InetAddress;

/**
 * Created by dpho on 3/12/16.
 * <p>
 * Information that this server will keep between each state.
 */
public class ElectionStateContext {
    private GossipManager manager;
    private ElectionSender sender;
    private InetAddress myAddr;
    private long term = 0;
    private boolean voted = false;

    public ElectionStateContext(GossipManager manager, ElectionSender sender) {
        this.manager = manager;
        this.sender = sender;
        this.myAddr = manager.getMe().getAddr();
    }

    public void incrementTerm() {
        setTerm(getTerm() + 1);
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean getVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public GossipManager getManager() {
        return manager;
    }

    public void setManager(GossipManager manager) {
        this.manager = manager;
    }

    public ElectionSender getSender() {
        return sender;
    }

    public void setSender(ElectionSender sender) {
        this.sender = sender;
    }

    public InetAddress getMyAddr() {
        return myAddr;
    }

    public void setMyAddr(InetAddress myAddr) {
        this.myAddr = myAddr;
    }
}
