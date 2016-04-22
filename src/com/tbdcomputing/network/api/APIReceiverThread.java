package com.tbdcomputing.network.api;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
import org.json.JSONObject;

import java.io.*;
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

                if (outputLine.equals("")) {
                    break;
                }

                out.println(outputLine);
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

            Runtime rt = Runtime.getRuntime();

            if(input.get("command").toString().equals("has_image")) {
                // check if this image is present by checking output of "docker images"
                String[] commands = {"docker", "images"};

                try {
                    Process proc = rt.exec(commands);

                    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    String line = null;

                    while( (line = br.readLine()) != null) {
                        if(line.contains(image)) {
                            // TODO: Return true
                            return "true";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if(input.get("command").toString().equals("run_image")) {
                String[] commands = {"docker", "start", image};

                try {
                    Process proc = rt.exec(commands);

                    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    String line = null;

                    while( (line = br.readLine()) != null) {
                        // TODO: Do something with container output
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
