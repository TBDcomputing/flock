package com.tbdcomputing.network.gossip;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;


/**
 * Object to keep track of relevant information for the gossip protocol. This
 * object has information such as IP, node status, and heart beat.
 * 
 * @author drew
 *
 */
public class GossipNode {
	private String uuid;
	private InetAddress addr;
	private int heartbeat;
	private long generationTime;
	private GossipStatus status;
	
	public GossipNode() {

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
		// TODO populate other fields as we populate the JSON
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
