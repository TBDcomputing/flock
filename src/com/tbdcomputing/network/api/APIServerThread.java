package com.tbdcomputing.network.api;

import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.gossip.GossipNode;
import com.tbdcomputing.network.gossip.GossipStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private APIServer apiServer;

    public APIServerThread(Socket accept, GossipManager manager, APIServer server) {
        super("APIServerThread");
        socket = accept;
        gossipManager = manager;
        apiServer = server;
        gossipManager.addObserver(this);
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
        } finally {
            apiServer.removeConnection(this);
            gossipManager.deleteObserver(this);
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
        } else if(inputLine.contains("leader")) {
            // TODO: get leader ip
        } else if(inputLine.contains("startelection")) {
            // TODO: run election

        } else if(inputLine.contains("broadcast")) {
            // TODO: parse command and send it to each node.

        }

        return "unsubscribe";
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO: add support for sending new node when GossipManager gets a new node by moving GossipListUtils in
        // to GossipManager.  Then calling the proper methods.  When addNode is called, fire off an observable event?
        // THen in here be an observer of the gossipmanager/electionmanager?  ALso fire off that event in gossipmanager.removeall.
        if(o == gossipManager) {
            GossipNode node = (GossipNode) arg;
            JSONObject json = node.toJSON();

            // TODO: send json to middleware
        }
    }
}
