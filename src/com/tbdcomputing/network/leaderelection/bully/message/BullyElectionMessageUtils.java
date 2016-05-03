package com.tbdcomputing.network.leaderelection.bully.message;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by dpho on 3/12/16.
 */
public class BullyElectionMessageUtils {
    public static JSONObject makeMessage(double alpha, InetAddress from, BullyElectionMessageType messageType) {
        JSONObject obj = new JSONObject();

        obj.put("alpha", alpha);
        obj.put("sender", from.getHostAddress());
        obj.put("type", messageType.name());

        return obj;
    }

    public static JSONObject makeMessage(String alphaConfigStr, double alpha, InetAddress from, BullyElectionMessageType messageType) {
        JSONObject obj = new JSONObject();

        obj.put("config", alphaConfigStr);
        obj.put("alpha", alpha);
        obj.put("sender", from.getHostAddress());
        obj.put("type", messageType.name());

        return obj;
    }

    public static double[] convertConfigStr(String configStr){
        String[] configStrs = configStr.split("\\s+");
        double[] config = new double[configStrs.length];
        for(int i = 0; i < configStrs.length; i++){
            config[i] = Double.valueOf(configStrs[i]);
        }
        return config;
    }
}
