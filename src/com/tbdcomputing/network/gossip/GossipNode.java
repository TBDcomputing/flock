package com.tbdcomputing.network.gossip;

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
	
	public GossipNode(InetAddress addr) {
		setUuid("");
		this.setAddr(addr);
		this.setHeartbeat(0);
		this.setGenerationTime(System.currentTimeMillis());
		setStatus(GossipStatus.STARTING);
	}

	public synchronized String getUuid() {
		return uuid;
	}

	public synchronized void setUuid(String uuid) {
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
