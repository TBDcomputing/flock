package com.tbdcomputing.network.api;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.gossip.GossipNode;
import com.tbdcomputing.network.gossip.GossipStatus;
import com.tbdcomputing.network.leaderelection.ElectionManager;
import com.tbdcomputing.network.leaderelection.state.ElectionState;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by drew on 4/6/16.
 */
public class APIServerThread extends Thread implements Observer {
    private Socket socket;
    private GossipManager gossipManager;
    private ElectionManager electionManager;
    private APIServer apiServer;

    public APIServerThread(Socket accept, GossipManager gossipManager, ElectionManager electionManager, APIServer server) {
        super("APIServerThread");
        socket = accept;
        this.gossipManager = gossipManager;
        this.electionManager = electionManager;
        apiServer = server;
        gossipManager.addObserver(this);
        electionManager.getElectionState().addObserver(this);

    }


    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            String inputLine, outputLine;
            while ((inputLine = in.readLine()) != null) {
                outputLine = processCommand(inputLine);
                out.println(outputLine);

                if (outputLine.equals("unsubscribe")) {
                    break;
                }
            }
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            apiServer.removeConnection(this);
            gossipManager.deleteObserver(this);
        }

    }

    private String processCommand(String inputLine) {
        JSONObject input = new JSONObject(inputLine);

        if (input.get("type").toString().equals("nodelist")) {

            List<GossipNode> nodes = gossipManager.getNodes();

            ArrayList<GossipNode> copyNodes = new ArrayList<>(nodes);
            copyNodes.add(gossipManager.getMe());

            copyNodes.stream().filter(node -> node.getStatus() != GossipStatus.LEAVING
                    && node.getStatus() != GossipStatus.DEAD);

            // Serialize and send our entire list of nodes
            // TODO: should we only send the IP?
            JSONArray json = new JSONArray(copyNodes.parallelStream().map(GossipNode::toJSON).toArray());
            return json.toString();
        } else if(input.get("type").toString().equals("leader")) {
            // get leader ip and send json back
            InetAddress leader = electionManager.getElectionState().getContext().getLeaderAddr();
            String ip = leader.getHostAddress().toString();
            JSONObject json = new JSONObject();
            json.put("type", "leader_ip");
            json.put("leader_ip", ip);

            return json.toString();
        } else if(input.get("type").toString().equals("start_election")) {
            // TODO: run election

            return "";
        } else if(input.get("type").toString().equals("has_image")) {
            // Parse image and send it to each node.
            String image = input.get("image").toString();
            List<GossipNode> nodes = gossipManager.getNodes();

            // TODO: Broadcast the image request to all nodes.
            for(GossipNode node : nodes) {
                JSONObject json = new JSONObject();
                json.put("type", "receive_command");
                json.put("image", image);

                // TODO: open tcp socket to another node and send to it.
//                ServerSocket nodeSocket = new ServerSocket(Constants.API_PORT)
            }

            return "";

        } else if(input.get("type").toString().equals("receive_command")) {
            // TODO: send back if you have the image


        } else if(input.get("type").toString().equals("flock_command")) {
            // TODO: do stuff for docker list?

        }

        return "unsubscribe";
    }

    @Override
    public void update(Observable o, Object arg) {
        JSONObject json;

        // Support sending new node when GossipManager gets a new node When addNode is called, observers are notified.
        if(o == gossipManager) {
            GossipNode node = (GossipNode) arg;
            json = node.toJSON();

        } else {
            // if (o == electionManager.getElectionState())
            // This node's leader changed

            ElectionState state = (ElectionState) arg;
            InetAddress addr = state.getContext().getLeaderAddr();
            json = new JSONObject();
            json.put("type", "new_leader");
            json.put("leader_ip", addr.getHostAddress().toString());

            // TODO: communicate with container to update the leader info
        }

        // send json to middleware
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(json.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
