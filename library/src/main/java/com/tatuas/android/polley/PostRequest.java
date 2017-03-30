package com.tatuas.android.polley;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PostRequest {

    private String mBoundaryCore;
    private String mRequestUrl;
    private List<RequestData.GetString> mGetStringData;
    private List<RequestData.PostString> mPostStringData;
    private List<RequestData.PostFile> mPostFileData;
    private HashMap<String, String> mAddHeaderData;

    private boolean mIsAddHeaderFlag = false;
    private int mReadTimeOut = 0;
    private int mConnectionTimeOut = 0;
    private String mEncodeCharType = "UTF-8";

    public PostRequest(String requestUrl) {
        mRequestUrl = requestUrl;
        mGetStringData = new ArrayList<>();
        mPostStringData = new ArrayList<>();
        mPostFileData = new ArrayList<>();
        mBoundaryCore = Long.toHexString(System.currentTimeMillis());
    }

    private String getRequestUrl() {
        List<String> pairs = new ArrayList<>();
        if (mGetStringData.size() > 0) {
            for (RequestData.GetString item : mGetStringData) {
                try {
                    String value = URLEncoder.encode(
                            item.getValue(),
                            mEncodeCharType
                    );
                    pairs.add(item.getKey() + "=" + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return mRequestUrl + "?" + implode(pairs, "&");
        } else {
            return mRequestUrl;
        }
    }

    public void addGetParam(String key, String value) {
        RequestData.GetString item = new RequestData.GetString(key, value);
        mGetStringData.add(item);
    }

    public void addPostParam(String key, String value) {
        RequestData.PostString item = new RequestData.PostString(key, value);
        mPostStringData.add(item);
    }

    public void addFileParam(String key, String mimeType, File uploadFile) {
        RequestData.PostFile item = new RequestData.PostFile(key, mimeType, uploadFile);
        mPostFileData.add(item);
    }

    public void addRequestHeader(HashMap<String, String> header) {
        mIsAddHeaderFlag = true;
        mAddHeaderData = header;
    }

    public void setCustomReadTimeOut(int millTime) {
        this.mReadTimeOut = millTime;
    }

    public void setCustomConnectionTimeOut(int millTime) {
        this.mConnectionTimeOut = millTime;
    }

    public void setCustomEncodeCharType(String type) {
        mEncodeCharType = type;
    }

    public PostResponse execute() {
        int FAILED_COMMON_RESPONSE_CODE = 0;

        // Create connection instance
        HttpURLConnection conn;
        try {
            URL url = new URL(getRequestUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" + mBoundaryCore
            );

            if (mIsAddHeaderFlag) {
                Set<String> keys = mAddHeaderData.keySet();
                for (String key : keys) {
                    conn.setRequestProperty(key, mAddHeaderData.get(key));
                }
            }
            conn.setDoOutput(true);
            conn.setAllowUserInteraction(true);
            conn.setConnectTimeout(mConnectionTimeOut);
            conn.setReadTimeout(mReadTimeOut);
            conn.setUseCaches(false);
        } catch (Exception e) {
            return new PostResponse(
                    convertErrorMessageToJsonString(
                            "Failed to create connection instance - " + e.toString()),
                    FAILED_COMMON_RESPONSE_CODE,
                    PostResponse.ResponseStatus.REQUEST_CONNECTION_FAILED
            );
        }

        // Connect to network
        try {
            conn.connect();
        } catch (Exception e) {
            // HttpURLConnection can be null.
            if (conn != null) {
                conn.disconnect();
            }
            return new PostResponse(
                    convertErrorMessageToJsonString(
                            "Failed to connect network - " + e.toString()),
                    FAILED_COMMON_RESPONSE_CODE,
                    PostResponse.ResponseStatus.REQUEST_NETWORK_ERROR
            );
        }

        // Exec Request
        int responseCode = FAILED_COMMON_RESPONSE_CODE;
        OutputStream requestStream;
        InputStream responseStream;
        try {
            // Send request
            requestStream = conn.getOutputStream();
            String boundaryString = "--" + mBoundaryCore + "\r\n";
            byte[] boundary = boundaryString.getBytes();

            requestStream.write(boundary);
            requestStream = insertPostStringDataBytes(requestStream);
            requestStream = insertPostFileDataBytes(requestStream);
            requestStream.write(boundary);
            requestStream.close();

            // Get response
            responseStream = conn.getInputStream();
            responseCode = conn.getResponseCode();
        } catch (Exception e) {
            // HttpURLConnection can be null.
            if (conn != null) {
                conn.disconnect();
            }
            return new PostResponse(
                    convertErrorMessageToJsonString(
                            "Failed to request - " + e.toString()),
                    responseCode,
                    PostResponse.ResponseStatus.REQUEST_DATA_ERROR);
        }

        // Parse Response
        try {
            return new PostResponse(
                    convertFromStreamToString(responseStream),
                    responseCode,
                    PostResponse.ResponseStatus.RESPONSE_COMPLETE);
        } catch (Exception e) {
            return new PostResponse(
                    convertErrorMessageToJsonString(
                            "Failed to parse response - " + e.toString()),
                    responseCode,
                    PostResponse.ResponseStatus.RESPONSE_DATA_ERROR);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String convertFromStreamToString(InputStream stream) throws Exception {
        InputStreamReader streamReader = new InputStreamReader(stream, mEncodeCharType);
        BufferedReader bufferReader = new BufferedReader(streamReader);
        String out = "";
        for (String line; (line = bufferReader.readLine()) != null; ) {
            out = out + line + "\n";
        }
        stream.close();
        bufferReader.close();
        return out;
    }

    private OutputStream insertPostStringDataBytes(OutputStream out) throws PostStringDataParseException {
        try {
            out.write(getPostStringDataBytes());
        } catch (Exception e) {
            throw new PostStringDataParseException(
                    "StringParseException:" + e.toString());
        }
        return out;
    }

    private OutputStream insertPostFileDataBytes(OutputStream out) throws PostFileDataParseException {
        try {
            File postFile;
            String mimeType;
            String inputTagFieldName;
            for (RequestData.PostFile file : mPostFileData) {
                postFile = file.getFile();
                mimeType = file.getMimeType();
                inputTagFieldName = file.getTagName();

                String fileInfoContent = "Content-Disposition: form-data; name=\"" +
                        inputTagFieldName +
                        "\"; filename=\"" +
                        postFile.getName() +
                        "\"\r\n" +
                        "Content-Type: " +
                        mimeType +
                        "\r\n" +
                        "\r\n";
                String boundaryContent = "\r\n" +
                        "\r\n" +
                        "--" +
                        mBoundaryCore +
                        "\r\n";

                out.write(fileInfoContent.getBytes());
                out.write(getPostDataBytes(postFile));
                out.write(boundaryContent.getBytes());
            }
        } catch (Exception e) {
            throw new PostFileDataParseException(
                    "FileParseException:" + e.toString());
        } catch (Error err) {
            throw new PostFileDataParseException(
                    "OutOfMemoryError:" + err.toString());
        }

        return out;
    }

    private byte[] getPostStringDataBytes() throws Exception {
        String dataString = "";
        for (RequestData.PostString pair : mPostStringData) {
            dataString = "Content-Disposition: form-data; name=\"" +
                    pair.getName() +
                    "\"\r\n" +
                    "\r\n" +
                    pair.getValue() +
                    "\r\n" +
                    "--" +
                    mBoundaryCore +
                    "\r\n";
        }

        return dataString.getBytes();
    }

    /**
     * If you want to custom upload byte[] override this method.
     *
     * @param file file
     * @return byte
     * @throws Exception Exception
     */
    public byte[] getPostDataBytes(File file) throws Exception {
        return getPostDataBytesFromFile(file);
    }

    public byte[] getPostDataBytesFromFile(File file) throws Exception {
        byte[] b = new byte[10];
        FileInputStream in;
        ByteArrayOutputStream out;

        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            while (in.read(b) > 0) {
                out.write(b);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            throw new Exception("UploadFileBytes : " + e.toString());
        } catch (Error err) {
            throw new Exception("OutOfMemory : " + err.toString());
        } finally {
            System.gc();
        }

        return out.toByteArray();
    }

    private class PostStringDataParseException extends Exception {
        PostStringDataParseException(String value) {
            super(value);
        }
    }

    private class PostFileDataParseException extends Exception {
        PostFileDataParseException(String value) {
            super(value);
        }
    }

    private String convertErrorMessageToJsonString(String msg) {
        return "{\"msg\":\"" + msg + "\"}";
    }

    private String implode(List<String> values, String glue) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int size = values.size();
        for (String value : values) {
            sb.append(value);
            if (index <= size - 2) {
                sb.append(glue);
            }
            index++;
        }
        return sb.toString();
    }
}
