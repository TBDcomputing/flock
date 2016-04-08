package com.tbdcomputing.network.api;

import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.gossip.GossipNode;
import com.tbdcomputing.network.gossip.GossipStatus;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by drew on 4/6/16.
 */
public class APIServerThread extends Thread {
    private Socket socket;
    private GossipManager gossipManager;

    public APIServerThread(Socket accept, GossipManager manager) {
        super("APIServerThread");
        socket = accept;
        gossipManager = manager;
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
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String processCommand(String inputLine) {
        if (inputLine.equals("nodelist")) {
            List<GossipNode> nodes = gossipManager.getNodes();

            ArrayList<GossipNode> copyNodes = new ArrayList<>(nodes);
            copyNodes.add(gossipManager.getMe());

            copyNodes.stream().filter(node -> node.getStatus() != GossipStatus.LEAVING
                    && node.getStatus() != GossipStatus.DEAD);

            // Serialize and send our entire list of nodes
            JSONArray json = new JSONArray(copyNodes.parallelStream().map(GossipNode::toJSON).toArray());
            return json.toString();
        } else if(inputLine.contains("start")) {
            // TODO: send command to master node
        }

        return "";
    }
}
