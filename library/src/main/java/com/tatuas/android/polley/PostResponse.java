package com.tatuas.android.polley;

import org.json.JSONException;
import org.json.JSONObject;

public class PostResponse {
    private String mResponse;
    private ResponseStatus mStatus;
    private int mResponseCode;

    public PostResponse(String response, int resultCode, ResponseStatus status) {
        mResponse = response;
        mResponseCode = resultCode;
        mStatus = status;
    }

    public String getResponseString() {
        return mResponse;
    }

    public JSONObject getResponseJson() throws JSONException {
        return new JSONObject(mResponse);
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public ResponseStatus getResponseStatus() {
        return mStatus;
    }

    public enum ResponseStatus {
        REQUEST_CONNECTION_FAILED,
        REQUEST_NETWORK_ERROR,
        REQUEST_DATA_ERROR,

        RESPONSE_COMPLETE,
        RESPONSE_DATA_ERROR
    }
}
