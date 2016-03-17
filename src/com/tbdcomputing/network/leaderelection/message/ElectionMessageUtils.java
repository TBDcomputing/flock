package com.tbdcomputing.network.leaderelection.message;

import org.json.JSONObject;

/**
 * Created by dpho on 3/12/16.
 */
public class ElectionMessageUtils {
    public JSONObject makeMessage(long term, ElectionMessageType messageType) {

        JSONObject obj = new JSONObject();
        obj.put("term", term);
        obj.put("id", "uuid?");

        switch (messageType) {
            case REQUESTVOTE:
                obj.put("type", "requestvote");
                break;
            case HEARTBEAT:
                obj.put("type", "heartbeat");
                break;
            case VOTEGRANTED:
                obj.put("type", "vote");
                break;
            case RESPONSE:
                obj.put("type", "response");
                break;
            default:
                obj = null;
        }

        return obj;
    }
}
