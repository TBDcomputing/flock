package com.tbdcomputing.network.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by drew on 4/20/16.
 */
public class APIReceiverThread extends Thread {
    private Socket socket;

    public APIReceiverThread(Socket accept) {
        super();
        socket = accept;
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
        }

    }

    public String processCommand(String inputLine) {
        JSONObject input = new JSONObject(inputLine);

        if (input.get("type").toString().equals("receive_command")) {
            String image = input.get("image").toString();

            // TODO: check if this image is present by checking output of docker ls?
        }
        return "";
    }
}
