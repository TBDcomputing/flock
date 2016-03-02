package com.tbdcomputing.network.gossip;

import com.tbdcomputing.network.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Object to keep track of relevant information for the gossip protocol. This
 * object has information such as IP, node status, and heart beat.
 *
 * @author drew
 */
public class GossipNode {
    private String uuid;
    private InetAddress addr;
    private int heartbeat;
    private long generationTime;
    private GossipStatus status;

    /**
     * This constructor should be used to populate the local GossipNode for this node. Otherwise, populate it with
     * GossipNode(JSONObject json) from the packet data.
     */
    public GossipNode() {
        this.setUUID(Constants.getUUID());
        // TODO populate other fields
    }

//	public GossipNode(InetAddress addr) {
//		setUUID("");
//		this.setAddr(addr);
//		this.setHeartbeat(0);
//		this.setGenerationTime(System.currentTimeMillis());
//		setStatus(GossipStatus.STARTING);
//	}

    public GossipNode(JSONObject json) throws JSONException {
        this.setUUID(json.getString("id"));
        try {
            this.setAddr(InetAddress.getByName(json.getString("address")));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.setHeartbeat(json.getInt("heartbeat"));
        this.setGenerationTime(json.getLong("generation_time"));
        // TODO populate other fields as we populate the JSON
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", uuid);
        obj.put("address", addr.getHostAddress());
        obj.put("heartbeat", heartbeat);
        obj.put("generation_time", generationTime);
        // TODO: add more data about this node to the JSONObject
        return obj;
    }

    public void update(GossipNode other) {
        this.addr = other.getAddr();
        this.heartbeat = other.getHeartbeat();
        this.generationTime = other.getGenerationTime();
        this.status = other.status;
    }

    @Override
    public String toString() {
        return "UUID: " + getUUID();
    }

    public synchronized String getUUID() {
        return uuid;
    }

    public synchronized void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public synchronized InetAddress getAddr() {
        return addr;
    }

    public synchronized void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public synchronized int getHeartbeat() {
        return heartbeat;
    }

    public synchronized void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public synchronized long getGenerationTime() {
        return generationTime;
    }

    public synchronized void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }

    public synchronized GossipStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(GossipStatus status) {
        this.status = status;
    }
}
