package com.tbdcomputing.network.api;

import com.tbdcomputing.network.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Created by drew on 4/20/16.
 */
public class APIReceiver {
    private ServerSocket serverSocket;
    private boolean listening;

    public APIReceiver() {

        try {
            serverSocket = new ServerSocket(Constants.API_INTERNAL_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listening = true;
    }

    public void run() {
        while (listening) {
            try {
                APIReceiverThread connection = new APIReceiverThread(serverSocket.accept());
                connection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
