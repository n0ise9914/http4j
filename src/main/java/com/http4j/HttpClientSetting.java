package com.http4j;

import java.util.List;
import java.util.Map;

public class HttpClientSetting {

    protected HttpClientCore core;
    protected String url;

    protected String httpVersion;
    protected Integer retries;

    public String cipherSuites;

    public List<String> tlsVersions;
    protected Integer callTimeout;
    protected Integer connectTimeout;
    protected Integer readTimeout;
    protected Integer writeTimeout;
    protected Boolean defaultHeaders;
    private Map<String, String> headers;
    private String proxy;

    public HttpClientSetting() {
        defaultHeaders = false;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Boolean getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(Boolean defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
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

    public HttpClientCore getCore() {
        return core;
    }

    public Integer getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public void setCore(HttpClientCore core) {
        this.core = core;
    }

    public Integer getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(Integer callTimeout) {
        this.callTimeout = callTimeout;
    }

    public String getCipherSuites() {
        return cipherSuites;
    }

    public void setCipherSuites(String cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    public List<String> getTlsVersions() {
        return tlsVersions;
    }

    public void setTlsVersions(List<String> tlsVersions) {
        this.tlsVersions = tlsVersions;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }
}
