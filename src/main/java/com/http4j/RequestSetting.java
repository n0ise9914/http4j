package com.http4j;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

public class RequestSetting extends ClientSetting {

    private String method;
    private String proxy;
    private Map<String, String> urlParameters;
    private Map<String, String> multipart;
    private byte[] body;
    private boolean skipResponseBody;

    public String getMethod() {
        return method;
    }

    public Map<String, String> getUrlParameters() {
        return urlParameters;
    }

    public Map<String, String> getMultipart() {
        return multipart;
    }

    public byte[] getBody() {
        return body;
    }

    public boolean shouldSkipResponseBody() {
        return skipResponseBody;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUrlParameters(Map<String, String> urlParameters) {
        this.urlParameters = urlParameters;
    }

    public void setMultipart(Map<String, String> multipart) {
        this.multipart = multipart;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setSkipResponseBody(boolean skipResponseBody) {
        this.skipResponseBody = skipResponseBody;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
}
