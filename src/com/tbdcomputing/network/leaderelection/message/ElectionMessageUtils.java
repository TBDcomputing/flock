package com.tbdcomputing.network.leaderelection.message;

import org.json.JSONObject;

/**
 * Created by dpho on 3/12/16.
 */
public class ElectionMessageUtils {
    public JSONObject makeMessage(long term, ElectionMessageType messageType) {
        if (messageType == ElectionMessageType.REQUESTVOTE) {

        } else if (messageType == ElectionMessageType.SUPPRESSION) {

        }
        return null;
    }
}
