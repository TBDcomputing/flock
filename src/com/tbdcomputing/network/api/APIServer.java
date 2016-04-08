package com.tbdcomputing.network.api;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


/**
 * Created by drew on 4/6/16.
 */
public class APIServer {
    private ServerSocket serverSocket;
    private GossipManager gossipManager;
    private boolean listening;

    public APIServer(GossipManager manager) {
        super();
        gossipManager = manager;
        listening = true;

        try {
            serverSocket = new ServerSocket(Constants.API_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (listening) {
            try {
                new APIServerThread(serverSocket.accept(), gossipManager).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
