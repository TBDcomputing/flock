package com.tbdcomputing.network.api;

import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.leaderelection.ElectionManager;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;



/**
 * Created by drew on 4/6/16.
 */
public class APIServer {
    private ServerSocket serverSocket;
    private GossipManager gossipManager;
    private ElectionManager electionManager;
    private List<APIServerThread> threads;
    private boolean listening;

    public APIServer(GossipManager gossipManager, ElectionManager electionManager) {
        super();
        this.gossipManager = gossipManager;
        this.electionManager = electionManager;

        listening = true;
        threads = new LinkedList<>();

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
                APIServerThread connection = new APIServerThread(serverSocket.accept(), gossipManager, electionManager, this);

                threads.add(connection);
                connection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void removeConnection(APIServerThread apiServerThread) {
        threads.remove(apiServerThread);
    }


}
