package com.tbdcomputing.network.leaderelection.bully.message;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by dpho on 3/12/16.
 */
public class BullyElectionMessageUtils {
    public static JSONObject makeMessage(String alpha, InetAddress from, BullyElectionMessageType messageType) {
        JSONObject obj = new JSONObject();

        obj.put("alpha", alpha);
        obj.put("sender", from.getHostAddress());
        obj.put("type", messageType.name());

        return obj;
    }
}
