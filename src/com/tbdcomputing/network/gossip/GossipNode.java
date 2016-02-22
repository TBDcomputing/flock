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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public InetAddress getAddr() {
		return addr;
	}

	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	public int getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(int heartbeat) {
		this.heartbeat = heartbeat;
	}

	public long getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(long generationTime) {
		this.generationTime = generationTime;
	}

	public GossipStatus getStatus() {
		return status;
	}

	public void setStatus(GossipStatus status) {
		this.status = status;
	}
}
