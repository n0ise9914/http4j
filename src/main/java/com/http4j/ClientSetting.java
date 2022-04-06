package com.http4j;

import java.util.Map;

public class ClientSetting {

    protected String url;
    protected Integer retries;
    protected Integer connectTimeout;
    protected Integer readTimeout;
    protected Boolean disableDefaultHeaders;
    private Map<String, String> headers;
    private String proxy;

    public ClientSetting() {
        disableDefaultHeaders = false;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Boolean shouldDisableDefaultHeaders() {
        return disableDefaultHeaders;
    }

    public void disableDefaultHeaders() {
        this.disableDefaultHeaders = true;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
