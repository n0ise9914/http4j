package com.http4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class HttpRequest {

    private final RequestSetting requestSetting;
    private final ClientSetting clientSetting;
    private int myRetries;

    public HttpRequest(ClientSetting clientSetting, RequestSetting requestSetting) {
        this.clientSetting = clientSetting;
        this.requestSetting = requestSetting;
    }

    public HttpRequest headers(Map<String, String> headers) {
        requestSetting.setHeaders(headers);
        return this;
    }

    public HttpRequest header(String name, String value) {
        if (requestSetting.getHeaders() == null) requestSetting.setHeaders(new Hashtable<>());
        requestSetting.getHeaders().put(name, value);
        return this;
    }

    public HttpRequest urlParams(Map<String, String> urlParameters) {
        requestSetting.setUrlParameters(urlParameters);
        return this;
    }

    public HttpRequest urlParam(String name, String value) {
        if (requestSetting.getUrlParameters() == null) requestSetting.setUrlParameters(new Hashtable<>());
        requestSetting.getHeaders().put(name, value);
        return this;
    }

    public HttpRequest skipResponseBody(boolean skipResponseBody) {
        requestSetting.setSkipResponseBody(skipResponseBody);
        return this;
    }

    public HttpRequest retries(int retries) {
        requestSetting.setRetries(retries);
        return this;
    }

    public HttpRequest multipart(Map<String, String> multipart) {
        requestSetting.setMultipart(multipart);
        return this;
    }

    public HttpRequest multipart(String name, String value) {
        if (requestSetting.getMultipart() == null) requestSetting.setMultipart(new Hashtable<>());
        requestSetting.getMultipart().put(name, value);
        return this;
    }

    public HttpRequest body(byte[] body) {
        requestSetting.setBody(body);
        return this;
    }

    public HttpRequest readTimeout(Integer milliseconds) {
        requestSetting.setReadTimeout(milliseconds);
        return this;
    }

    public HttpRequest connectTimeout(Integer milliseconds) {
        requestSetting.setConnectTimeout(milliseconds);
        return this;
    }

    public HttpRequest proxy(String proxy) {
        requestSetting.setProxy(proxy);
        return this;
    }

    public HttpResponse execute() {
        HttpResponse resp = new HttpResponse();
        HttpURLConnection con = null;
        String method = requestSetting.getMethod();
        Map<String, String> headers = getHeaders();
        try {
            appendMultipart();
            byte[] body = this.requestSetting.getBody();
            if (method.startsWith("P")) {
                headers.put("Content-Length", body == null ? "0" : String.valueOf(body.length));
            }
            URL _url = new URL(getUrl());
            String proxyStr = getProxy();
            if (proxyStr != null && proxyStr.contains(":")) {
                String[] proxyArr = requestSetting.getProxy().split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyArr[0], Integer.parseInt(proxyArr[1])));
                con = (HttpURLConnection) _url.openConnection(proxy);
            } else {
                con = (HttpURLConnection) _url.openConnection();
            }
            for (Map.Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setConnectTimeout(getConnectTimeout());
            con.setReadTimeout(getReadTimeout());
            con.setRequestMethod(method);
            con.setDoOutput(method.startsWith("P"));
            con.setDoInput(true);
            con.connect();
            if (method.startsWith("P")) {
                if (body == null) body = new byte[]{};
                OutputStream os = con.getOutputStream();
                os.write(body);
                os.flush();
                os.close();
            }
            byte[] respBody = null;
            if (!requestSetting.shouldSkipResponseBody()) {
                respBody = Utils.unwrapBody(con.getInputStream());
            } else {
                con.getInputStream().close();
            }
            //con.getErrorStream();
            resp.headers = con.getHeaderFields();
            if (respBody != null) {
                resp.body = new String(respBody, StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            resp.error = ex;
            try {
                if (con != null) {
                    resp.body = new String(Utils.unwrapBody(con.getErrorStream()));
                }
            } catch (Exception ignored) {
            }
            // ex.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    resp.status = con.getResponseCode();
                } catch (IOException ignored) {
                }
                con.disconnect();
            }
        }
        if (resp.status == 0 && myRetries < getRetries() - 1) {
            myRetries++;
            return execute();
        }
        return resp;
    }

    private String getProxy() {
        String proxy = null;
        if (requestSetting.getProxy() != null && requestSetting.getProxy().contains(":")) {
            proxy = requestSetting.getProxy();
        } else if (clientSetting.getProxy() != null && clientSetting.getProxy().contains(":")) {
            proxy = clientSetting.getProxy();
        }
        return proxy;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new Hashtable<>();
        if (!clientSetting.shouldDisableDefaultHeaders())
            headers.putAll(getDefaultHeaders());
        if (clientSetting.getHeaders() != null)
            headers.putAll(clientSetting.getHeaders());
        if (clientSetting.getHeaders() != null)
            headers.putAll(requestSetting.getHeaders());
        return headers;
    }

    public Map<String, String> getDefaultHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("user-agent", "okhttp/4.9.3");
        map.put("Accept", "*/*");
        map.put("Connection", "keep-alive");
        map.put("Cache-Control", "no-cache");
        //map.put("Accept-Encoding", "gzip, deflate, br");
        return map;
    }

    private String getUrl() {
        StringBuilder uri = new StringBuilder();
        if (requestSetting.getUrl() == null) {
            uri.append(clientSetting.getUrl());
        } else if (requestSetting.getUrl().startsWith("http")) {
            uri.append(requestSetting.getUrl());
        } else {
            uri.append(clientSetting.getUrl());
            uri.append(requestSetting.getUrl());
        }
        Map<String, String> urlParameters = requestSetting.getUrlParameters();
        if (urlParameters != null && urlParameters.size() != 0) {
            uri.append("?");
            for (Map.Entry<String, String> parameter : urlParameters.entrySet()) {
                uri.append(parameter.getKey()).append("=");
                uri.append(URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8));
            }
        }
        return uri.toString();
    }

    private Integer getConnectTimeout() {
        if (requestSetting.getConnectTimeout() != null) {
            return requestSetting.getConnectTimeout();
        } else if (clientSetting.getConnectTimeout() != null) {
            return clientSetting.getConnectTimeout();
        } else {
            return 3_000;
        }
    }

    private Integer getReadTimeout() {
        if (requestSetting.getReadTimeout() != null) {
            return requestSetting.getReadTimeout();
        } else if (clientSetting.getReadTimeout() != null) {
            return clientSetting.getReadTimeout();
        } else {
            return 5_000;
        }
    }

    private Integer getRetries() {
        if (requestSetting.getRetries() != null) {
            return requestSetting.getRetries();
        } else if (clientSetting.getRetries() != null) {
            return clientSetting.getRetries();
        } else {
            return 0;
        }
    }

    private void appendMultipart() {
        Map<String, String> multipart = requestSetting.getMultipart();
        if (multipart == null || multipart.size() == 0) {
            return;
        }
        String multipartId = Utils.createMultipartId(24);
        StringBuilder sb = new StringBuilder();
        multipart.forEach((key, value) -> {
            sb.append("----------------------------").append(multipartId).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
            sb.append(value).append("\r\n");
        });
        sb.append("----------------------------").append(multipartId).append("--\r\n");
        requestSetting.setBody(sb.toString().getBytes(StandardCharsets.UTF_8));
        requestSetting.getHeaders().put("Content-Type", "multipart/form-data; boundary=--------------------------" + multipartId);
    }
}
