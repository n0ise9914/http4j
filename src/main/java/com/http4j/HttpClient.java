package com.http4j;

public class HttpClient {

    public static final class Builder {
        private final ClientSetting setting;

        public Builder() {
            setting = new ClientSetting();
        }

        public Builder baseUrl(String baseUrl) {
            setting.setUrl(baseUrl);
            return this;
        }

        public Builder retries(Integer retries) {
            setting.setRetries(retries);
            return this;
        }

        public Builder disableDefaultHeaders() {
            setting.disableDefaultHeaders();
            return this;
        }

        public Builder readTimeout(Integer milliseconds) {
            setting.setReadTimeout(milliseconds);
            return this;
        }

        public Builder connectTimeout(Integer milliseconds) {
            setting.setConnectTimeout(milliseconds);
            return this;
        }

        public Builder proxy(String proxy) {
            setting.setProxy(proxy);
            return this;
        }

        public HttpClient build() {
            return new HttpClient(setting);
        }
    }

    private final ClientSetting setting;

    private HttpClient(ClientSetting setting) {
        this.setting = setting;
    }

    public static Builder builder(){
        return new Builder();
    }

    public HttpRequest post(String url) {
        return createRequest(url, "POST");
    }

    public HttpRequest get(String url) {
        return createRequest(url, "GET");
    }

    public HttpRequest head(String url) {
        return createRequest(url, "HEAD");
    }

    public HttpRequest options(String url) {
        return createRequest(url, "OPTIONS");
    }

    public HttpRequest put(String url) {
        return createRequest(url, "PUT");
    }

    public HttpRequest delete(String url) {
        return createRequest(url, "DELETE");
    }

    public HttpRequest trace(String url) {
        return createRequest(url, "TRACE");
    }

    public HttpRequest patch(String url) {
        return createRequest(url, "PATCH");
    }

    private HttpRequest createRequest(String url, String method) {
        RequestSetting setting = new RequestSetting();
        setting.setMethod(method);
        setting.setUrl(url);
        return new HttpRequest(this.setting, setting);
    }


}
