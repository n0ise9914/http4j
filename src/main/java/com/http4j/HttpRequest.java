package com.http4j;

import okhttp3.*;
import okhttp3.internal.http2.Header;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpRequest {

    private final RequestSetting requestSetting;
    private final HttpClientSetting clientSetting;
    private final HttpClient httpClient;
    private int myRetries;

    public HttpRequest(HttpClient httpClient, HttpClientSetting clientSetting, RequestSetting requestSetting) {
        this.clientSetting = clientSetting;
        this.requestSetting = requestSetting;
        this.httpClient = httpClient;
    }

    public HttpRequest headers(Map<String, String> headers) {
        requestSetting.setHeaders(headers);
        return this;
    }

    public HttpRequest defaultHeaders(Boolean enabled) {
        this.clientSetting.defaultHeaders = enabled;
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
        requestSetting.getUrlParameters().put(name, value);
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

    public HttpResponse execute() throws Exception {
        if (clientSetting.getCore() == null || clientSetting.getCore() == HttpClientCore.Java8) {
            return executeJava8();
        } else if (clientSetting.getCore() == HttpClientCore.Okhttp) {
            return executeOkhttp();
        } else if (clientSetting.getCore() == HttpClientCore.Java11) {
            return executeJava11();
        }
        return null;
    }

    private HttpResponse executeJava11() throws Exception {
        HttpResponse resp = new HttpResponse();
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(new URI(requestSetting.url));
        if (requestSetting.callTimeout != null) {
            builder.timeout(Duration.ofSeconds(requestSetting.callTimeout));
        }
        if (getHttpVersion() != null) {
            builder.version(java.net.http.HttpClient.Version.valueOf(getHttpVersion()));
        }
        getHeaders().forEach(builder::header);
        switch (requestSetting.getMethod()) {
            case "GET" -> builder.GET();
            case "PUT" -> builder.PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(requestSetting.getBody()));
            case "POST" -> builder.POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(requestSetting.getBody()));
            case "DELETE" -> builder.DELETE();
        }
        java.net.http.HttpRequest request = builder.build();
        java.net.http.HttpResponse<String> response = httpClient.java11HttpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        resp.status = response.statusCode();
        resp.headers = response.headers().map();
        resp.body = response.body().getBytes(StandardCharsets.UTF_8);
        return resp;
    }


    private HttpResponse executeOkhttp() throws Exception {
        HttpResponse resp = new HttpResponse();
        Request.Builder req = new Request.Builder()
                .url(requestSetting.url);
        getHeaders().forEach(req::header);
        switch (requestSetting.getMethod()) {
            case "GET" -> req.get();
            case "PUT" -> req.put(RequestBody.create(requestSetting.getBody()));
            case "POST" -> req.post(RequestBody.create(requestSetting.getBody()));
            case "DELETE" -> req.delete();
        }
        try (Response response = httpClient.okHttpClient.newCall(req.build()).execute()) {
            resp.status = response.code();
            resp.headers = response.headers().toMultimap();
            ResponseBody body = response.body();
            if (body != null) {
                resp.body = body.bytes();
            }
        }
        return resp;
    }

    public HttpResponse executeJava8() throws Exception {
        HttpResponse resp = new HttpResponse();
        HttpURLConnection con = null;
        String method = requestSetting.getMethod();
        Map<String, String> headers = getHeaders();
        //long startTime = Instant.now().getEpochSecond();
//        try {
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
        //con.getErrorStream();
        resp.status = con.getResponseCode();
        resp.headers = con.getHeaderFields();
        if (con.getResponseCode() != -1) {
            if (!requestSetting.shouldSkipResponseBody()) {
                respBody = Utils.unwrapBody(con.getInputStream());
            } else {
                con.getInputStream().close();
            }
            if (respBody != null) {
                resp.body = respBody;
            }
        }
//        }
//        catch (Exception ex) {
//            resp.error = ex;
//            try {
        if (con != null && con.getErrorStream() != null) {
            resp.body = Utils.unwrapBody(con.getErrorStream());
            resp.status = con.getResponseCode();
            throw new Exception(new String(resp.body));
        }
//            } catch (Exception ignored) {
//            }
        // ex.printStackTrace();
//        } finally {
//            if (con != null) {
//                con.disconnect();
//            }
//        }
        if (resp.status == 0 && myRetries < getRetries() - 1) {
            myRetries++;
            return executeJava8();
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
        if ((clientSetting.defaultHeaders == null || clientSetting.defaultHeaders) && (requestSetting.defaultHeaders == null || requestSetting.defaultHeaders))
            headers.putAll(getDefaultHeaders());
        if (clientSetting.getHeaders() != null) headers.putAll(clientSetting.getHeaders());
        if (requestSetting.getHeaders() != null) headers.putAll(requestSetting.getHeaders());
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

    private String getHttpVersion() {
        if (requestSetting.httpVersion != null) {
            return requestSetting.httpVersion;
        } else if (clientSetting.httpVersion != null) {
            return clientSetting.httpVersion;
        } else {
            return null;
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
        for (Map.Entry<String, String> entry : multipart.entrySet()) {
            sb.append("----------------------------").append(multipartId).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
            sb.append(entry.getValue()).append("\r\n");
        }
        sb.append("----------------------------").append(multipartId).append("--\r\n");
        requestSetting.setBody(sb.toString().getBytes(StandardCharsets.UTF_8));
        requestSetting.getHeaders().put("Content-Type", "multipart/form-data; boundary=--------------------------" + multipartId);
    }
}
