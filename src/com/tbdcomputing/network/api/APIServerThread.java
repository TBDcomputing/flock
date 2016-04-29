package com.tbdcomputing.network.api;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.gossip.GossipNode;
import com.tbdcomputing.network.gossip.GossipStatus;
import com.tbdcomputing.network.leaderelection.ElectionManager;
import com.tbdcomputing.network.leaderelection.bully.BullyElectionManager;
import com.tbdcomputing.network.leaderelection.bully.state.BullyElectionState;
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
    private BullyElectionManager electionManager;

    public APIServerThread(Socket accept, GossipManager gossipManager, BullyElectionManager electionManager) {
        super("APIServerThread");
        socket = accept;
        this.gossipManager = gossipManager;
        this.electionManager = electionManager;
        gossipManager.addObserver(this);

        // TODO: Fix in election manager
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

                if (outputLine.equals("unsubscribe")) {
                    break;
                }

                if(!outputLine.equals("")) {
                    out.println(outputLine);
                }
            }
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            gossipManager.deleteObserver(this);
            electionManager.getElectionState().deleteObserver(this);
        }

    }

    public String processCommand(String inputLine) {
        JSONObject input = new JSONObject(inputLine);

        if (input.get("type").toString().equals("nodelist")) {

            List<GossipNode> nodes = gossipManager.getNodes();

            ArrayList<GossipNode> copyNodes = new ArrayList<>(nodes);
            copyNodes.add(gossipManager.getMe());

            copyNodes.stream().filter(node -> node.getStatus() != GossipStatus.LEAVING
                    && node.getStatus() != GossipStatus.DEAD);

            // Serialize and send our entire list of nodes
            // TODO: should we only send the IP?
            JSONObject jsonObject = new JSONObject();
            JSONArray json = new JSONArray(copyNodes.parallelStream().map(GossipNode::toJSON).toArray());
            jsonObject.put("type", "nodelist");
            jsonObject.put("nodes", json);
            return jsonObject.toString();
        } else if(input.get("type").toString().equals("leader")) {
            // get leader ip and send json back
            InetAddress leader = electionManager.getElectionState().getContext().getLeaderAddr();
            String ip = leader.getHostAddress().toString();
            JSONObject json = new JSONObject();
            json.put("type", "leader_ip");
            json.put("leader_ip", ip);

            return json.toString();
        } else if(input.get("type").toString().equals("start_election")) {
            electionManager.startElection();

            return "";
        } else if(input.get("type").toString().equals("has_image")) {
            // Parse image and send it to each node.
            String image = input.get("image").toString();
            List<GossipNode> nodes = new ArrayList<>(gossipManager.getNodes());
            nodes.add(gossipManager.getMe());

            List<GossipNode> nodesWithImage = new ArrayList<>();

            JSONObject json = new JSONObject();
            json.put("type", "receive_command");
            json.put("command", "has_image");
            json.put("image", image);
            
            // Broadcast the has_image request to all nodes.
            for(GossipNode node : nodes) {
                try {
                    // Open tcp socket to another node and send to it.
                    Socket nodeSocket = new Socket(node.getAddr(), Constants.API_INTERNAL_PORT);

                    // Send the json to that node.
                    PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));

                    out.println(json.toString());

                    // Get back result of that command.
                    String result = in.readLine();

                    // If that came back true, add node to list of nodes with that image.
                    if(result.equals("true")) {
                        nodesWithImage.add(node);
                    }

                    nodeSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // Return array of nodes that have the image
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonResult = new JSONArray(nodesWithImage.parallelStream().map(GossipNode::toJSON).toArray());
            jsonObject.put("type", "has_image");
            jsonObject.put("nodes", jsonResult);
            return jsonObject.toString();

        } else if(input.get("type").toString().equals("run_image")) {
            // TODO: Start image on all nodes that have this image
            // TODO: get back container info from nodes that we send this command to.
            String image = input.get("image").toString();
            List<GossipNode> nodes = new ArrayList<>(gossipManager.getNodes());
            nodes.add(gossipManager.getMe());
//            JSONArray nodesText = input.getJSONArray("nodes");
//
//            // TODO: Get IP Addresses from all nodes in nodesText
//            List<GossipNode> nodes = new ArrayList<>();
//            for(int i = 0; i < nodesText.length(); i++) {
//                nodes.add(new GossipNode(nodesText.getJSONObject(i)));
//            }

            List<String> nodeContainers = new ArrayList<>();
            JSONObject json = new JSONObject();
            json.put("type", "receive_command");
            json.put("command", "run_image");
            json.put("image", image);

            // Broadcast the run_image request to all nodes.
            for(GossipNode node : nodes) {
                try {
                    // Open tcp socket to another node and send to it.
                    Socket nodeSocket = new Socket(node.getAddr(), Constants.API_INTERNAL_PORT);

                    // Send the json to that node.
                    PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));

                    out.println(json.toString());

                    // Add returned container information to JSONAray.
                    String result = in.readLine();

                    if (!"".equals(result)) {
                        nodeContainers.add(result);
                    }

                    nodeSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // Return array of nodes that have the image
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonResult = new JSONArray(nodeContainers.toArray());
            jsonObject.put("type", "run_image");
            jsonObject.put("nodes", jsonResult);
            return jsonObject.toString();
        } else if(input.getString("type").equals("stop_image")) {
            // Parse image and send it to each node.
            String image = input.get("image").toString();
            List<GossipNode> nodes = new ArrayList<>(gossipManager.getNodes());
            nodes.add(gossipManager.getMe());

            JSONObject json = new JSONObject();
            json.put("type", "receive_command");
            json.put("command", "stop_image");
            json.put("image", image);

            // Broadcast the stop_image request to all nodes.
            for(GossipNode node : nodes) {
                try {
                    // Open tcp socket to another node and send to it.
                    Socket nodeSocket = new Socket(node.getAddr(), Constants.API_INTERNAL_PORT);

                    // Send the json to that node.
                    PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));

                    out.println(json.toString());

                    nodeSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return "";

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
            json.put("type", "new_node");

        } else {
            // if (o == electionManager.getElectionState())
            // This node's leader changed

            BullyElectionState state = (BullyElectionState) arg;
            InetAddress addr = state.getContext().getLeaderAddr();
            json = new JSONObject();
            json.put("type", "new_leader");
            json.put("leader_ip", addr.getHostAddress().toString());
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
