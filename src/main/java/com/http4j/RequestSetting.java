package com.http4j;

import java.util.Map;

public class RequestSetting extends ClientSetting {

    private String method;
    private Map<String, String> urlParameters;
    private Map<String, String> multipart;
    private byte[] body;
    private boolean skipResponseBody;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getUrlParameters() {
        return urlParameters;
    }

    public void setUrlParameters(Map<String, String> urlParameters) {
        this.urlParameters = urlParameters;
    }

    public Map<String, String> getMultipart() {
        return multipart;
    }

    public void setMultipart(Map<String, String> multipart) {
        this.multipart = multipart;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean shouldSkipResponseBody() {
        return skipResponseBody;
    }

    public void setSkipResponseBody(boolean skipResponseBody) {
        this.skipResponseBody = skipResponseBody;
    }


}
