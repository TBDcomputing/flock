package com.tbdcomputing.network.api;

import com.sun.corba.se.spi.activation.Server;
import com.sun.tools.javac.file.JavacFileManager;
import com.tbdcomputing.network.Constants;
import com.tbdcomputing.network.gossip.GossipManager;
import com.tbdcomputing.network.leaderelection.ElectionManager;
import com.tbdcomputing.network.leaderelection.bully.BullyElectionManager;


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
    private BullyElectionManager electionManager;
    private APIReceiver receiver;
    private Thread receiverThread;
    private boolean listening;

    public APIServer(GossipManager gossipManager, BullyElectionManager electionManager) {
        this.gossipManager = gossipManager;
        this.electionManager = electionManager;

        receiver = new APIReceiver();
        receiverThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    receiver.run();
                }
                System.err.println("Exiting receiver thread!");
            }
        };
        receiverThread.start();

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
                APIServerThread connection = new APIServerThread(serverSocket.accept(), gossipManager, electionManager, this);

                connection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public ServerSocket getSocket() {
        return serverSocket;
    }
}
