package com.tbdcomputing.network.leaderelection.bully.state;

import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.leaderelection.ElectionSender;

import java.net.InetAddress;

/**
 * Created by dpho on 3/12/16.
 * <p>
 * Information that this server will keep between each state.
 */
public class BullyElectionStateContext {
    private GossipManager manager;
    private ElectionSender sender;
    private InetAddress myAddr;
    private InetAddress leaderAddr;

    public BullyElectionStateContext(GossipManager manager, ElectionSender sender) {
        this.manager = manager;
        this.sender = sender;
        this.myAddr = manager.getMe().getAddr();
    }

    public String getAlpha() {
        //TODO: Temporary fake placeholder
        return manager.getMe().getAddr().getHostAddress();
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

    public InetAddress getLeaderAddr() {
        return leaderAddr;
    }

    public void setLeaderAddr(InetAddress leaderAddr) {
        this.leaderAddr = leaderAddr;
    }
}
