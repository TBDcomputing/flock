package com.tbdcomputing.network.api;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
import com.sun.tools.javac.parser.Scanner;
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

            if (input.get("command").toString().equals("has_image")) {
                if (hasImage(image)) {
                    return "true";
                } else {
                    return "false";
                }
            } else if (input.get("command").toString().equals("run_image")) {
                if (!hasImage(image)) {
                    return "";
                }

                Runtime rt = Runtime.getRuntime();

                String[] commands = {"./flock_docker.sh", "start", image, "8895"};

                try {
                    Process proc = rt.exec(commands);

                    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    String line = null;

                    // TODO: return json with ip and port
                    while ((line = br.readLine()) != null) {
                        // TODO: Do something with container output
                        System.out.println(line);
                    }
                    JSONObject json = new JSONObject();
                    json.put("ip", socket.getInetAddress().getHostAddress().toString());
                    json.put("port", "8895");
                    return json.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public boolean hasImage(String image) {
        // check if this image is present by checking output of "docker images"
        String[] commands = {"docker", "images"};
        Runtime rt = Runtime.getRuntime();

        try {
            Process proc = rt.exec(commands);

            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = null;

            String[] imageForm = image.split(":");

            boolean containsImage = false;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\s+");

                if (splitLine.length < imageForm.length) continue;

                containsImage = true;

                // Handle "centos:7" by splitting on colon and checking if it contains both name and tag
                for (int i = 0; i < imageForm.length; i++) {
                    if (!splitLine[i].equals(imageForm[i])) {
                        containsImage = false;
                    }
                }

                if (containsImage) break;
            }

            if (containsImage) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

//    public static void main(String[] args) {
//        JSONObject json = new JSONObject();
//        json.put("type", "receive_command");
//        json.put("image", "centos:7");
//        json.put("command", "run_image");
//        System.out.println("Process Command Result: " + processCommand(json.toString()));
//    }
}
