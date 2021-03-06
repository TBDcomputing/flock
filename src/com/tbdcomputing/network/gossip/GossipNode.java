package com.tbdcomputing.network.gossip;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.Flock;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;


/**
 * Object to keep track of relevant information for the gossip protocol. This
 * object has information such as IP, node status, and heart beat.
 *
 * @author drew
 */
public class GossipNode {
    private String uuid;
    private InetAddress addr;
    private long heartbeat;
    private long generationTime;
    private GossipStatus status;
    private Alpha alpha;

    /**
     * This constructor should be used to populate the local GossipNode for this node. Otherwise, populate it with
     * GossipNode(JSONObject json) from the packet data.
     */
    public GossipNode() {
        this.setUUID(Constants.getUUID());
        // TODO populate other fields
        this.heartbeat = System.currentTimeMillis();
        this.generationTime = this.heartbeat;
        this.status = GossipStatus.NORMAL; // TODO: change to starting and update lifecycle of GossipNode to update status.
        this.alpha = new Alpha();

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
        this.setHeartbeat(json.getLong("heartbeat"));
        this.setGenerationTime(json.getLong("generation_time"));
        // TODO populate other fields as we populate the JSON
        this.setStatus(GossipStatus.valueOf(json.getString("status")));
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", uuid);
        obj.put("address", addr.getHostAddress());
        obj.put("heartbeat", heartbeat);
        obj.put("generation_time", generationTime);
        obj.put("status", status.toString());
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

    @Override
    public int hashCode() {
        return this.getUUID().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        GossipNode oth = (GossipNode) o;
        return this.getUUID().equals(oth.getUUID());
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

    public synchronized long getHeartbeat() {
        return heartbeat;
    }

    public synchronized void setHeartbeat(long heartbeat) {
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

    public synchronized double getAlphaValue() { return this.alpha.getAlphaValue(); }

    private class Alpha implements Comparable {

        static final int NUM_TRIALS = 5;

        double alphaValue;
        double latencyAvg;
        double loadAvg;
        double uptimeAvg;
        double throughputAvg;

        public Alpha(){
            Flock.getOS();
            refreshAlpha();
            //TODO add mac support
        }

        public Alpha(JSONObject configuration){
            //TODO read in a configuration somewhere that provides weights for the alpha factors
        }

        public void refreshAlpha(){
            latencyAvg = getLatencyAvg();

            loadAvg = getLoadAvg();
            uptimeAvg = getUptimeAvg();
            throughputAvg = getThroughputAvg();

            System.out.println("latency: "+ latencyAvg);
            System.out.println("loadavg: "+loadAvg);
            System.out.println("uptimeavg: "+uptimeAvg);

            latencyAvg = ((latencyAvg) / (500));
            loadAvg = ((loadAvg) / (3));
            uptimeAvg = uptimeAvg/1000;
            uptimeAvg = ((uptimeAvg) / (3600));

            alphaValue = throughputAvg + uptimeAvg + (-1*loadAvg) + (-1*latencyAvg);
            System.out.println("alpha: " + alphaValue);

        }

        /**
         * @return The average latency to stackoverflow.com for this node
         */
        public double getLatencyAvg(){

            //For now, we cannot use latency, since the demo is on Andriy's router
            return 350;
//            String[] latencyAvgCmd = new String[]{"bash","-c","ping -c 4 www.stackoverflow.com | tail -1| awk '{print $4}' | cut -d '/' -f 2"};
//            try {
//                Process p = Runtime.getRuntime().exec(latencyAvgCmd);
//
//                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//                String[] output = in.readLine().split("\\s+");
//
//                if(output.length > 1 && output[1].equals("unknown")){
//                    return 500;
//                }
//
//                in.close();
//
//                return Double.parseDouble(output[0]);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                return 500;
//            }
        }

        /**
         * @return The average load over the past 15 minutes
         */
        public double getLoadAvg() {
            if(Flock.os == Flock.OS.LINUX){
                String[] loadAvgCmd = new String[]{"bash","-c","cat /proc/loadavg"};
                try {
                    Process p = Runtime.getRuntime().exec(loadAvgCmd);

                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String[] output = in.readLine().split("\\s+");

                    in.close();

                    return Double.parseDouble(output[2]);

                } catch (IOException e) {
                    e.printStackTrace();
                    return 5;
                }
            }else{
                //TODO get equivalent command for mac
                return 5;
            }

        }

        public double getThroughputAvg(){
            //computational throughput: run a task that measures it, the task should do some multithreaded assignment
            //the task should have a start time and an end time
            //the task should do some multithreaded work over the period of time endTime - startTime
            //The multithreaded work should use 10 threads to do some heavy task
            //The task should ideally not require extra memory like a merge sort
            return 0;
        }

        public double getUptimeAvg(){
            try{
                File file = new File(Constants.ALPHA_UPTIME_LOG);

                if (!file.exists()) {
                    Flock.startTime = System.currentTimeMillis();
                    return 1000;
                }else{
                    FileReader fileReader = new FileReader(Constants.ALPHA_UPTIME_LOG);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    long avgUptime = Long.parseLong(bufferedReader.readLine());
                    bufferedReader.close();
                    return avgUptime;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public double getAlphaValue(){
            return alphaValue;
        }

        @Override
        public int compareTo(Object o) {
            Alpha that = (Alpha) o;
            if (this.alphaValue > that.alphaValue) {
                return 1;
            } else if (this.alphaValue < that.alphaValue) {
                return -1;
            } else {
                return 0;
            }
        }


        @Override
        public String toString() {
            return String.valueOf(this.alphaValue);
        }
    }
}
