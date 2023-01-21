package com.http4j;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

import javax.net.ssl.*;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class HttpClient {

    private final HttpClientSetting setting;
    protected OkHttpClient okHttpClient;
    private ExecutorService executor;

    protected java.net.http.HttpClient java11HttpClient;

    private HttpClient(HttpClientSetting setting) {
        this.setting = setting;
        if (setting.getCore() == null) {
            //skip
        } else if (setting.getCore() == HttpClientCore.Java8) {
            //skip
        } else if (setting.getCore() == HttpClientCore.Okhttp) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (setting.connectTimeout != null) {
                builder.connectTimeout(setting.connectTimeout, TimeUnit.SECONDS);
            }
            if (setting.writeTimeout != null) {
                builder.writeTimeout(setting.writeTimeout, TimeUnit.SECONDS);
            }
            if (setting.readTimeout != null) {
                builder.readTimeout(setting.readTimeout, TimeUnit.SECONDS);
            }
            if (setting.callTimeout != null) {
                builder.callTimeout(setting.callTimeout, TimeUnit.SECONDS);
            }
            try {
                List<CipherSuite> customCipherSuites = new ArrayList<>();
                if (setting.cipherSuites != null) {
                    for (String cipherSuite : setting.cipherSuites) {
                        customCipherSuites.add(CipherSuite.forJavaName(cipherSuite));
                    }
                }
                ConnectionSpec.Builder specBuilder = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .cipherSuites(customCipherSuites.toArray(new CipherSuite[0]));
                if (setting.tlsVersions != null) {
                    for (String tlsVersion : setting.tlsVersions) {
                        specBuilder.tlsVersions(TlsVersion.forJavaName(tlsVersion));
                    }
                }
                ConnectionSpec spec = specBuilder.build();
                X509TrustManager trustManager = defaultTrustManager();
                SSLSocketFactory sslSocketFactory = defaultSslSocketFactory(trustManager);
                SSLSocketFactory customSslSocketFactory = new DelegatingSSLSocketFactory(sslSocketFactory) {
                    @Override
                    protected SSLSocket configureSocket(SSLSocket socket) {
                        socket.setEnabledCipherSuites(javaNames(Objects.requireNonNull(spec.cipherSuites())));
                        return socket;
                    }
                };
                builder.sslSocketFactory(customSslSocketFactory, trustManager);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            okHttpClient = builder.build();
        } else if (setting.getCore() == HttpClientCore.Java11) {
            //this.executor = createCachedThreadPool();
            java.net.http.HttpClient.Builder builder = java.net.http.HttpClient
                    .newBuilder()
                    //.executor(executor)
                    ;
            if (setting.connectTimeout != null) {
                builder.connectTimeout(Duration.of(setting.connectTimeout, ChronoUnit.SECONDS));
            }
            java11HttpClient = builder.build();
        }
    }

    private String[] javaNames(List<CipherSuite> cipherSuites) {
        String[] result = new String[cipherSuites.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cipherSuites.get(i).javaName();
        }
        return result;
    }

    private SSLSocketFactory defaultSslSocketFactory(X509TrustManager trustManager)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, null);

        return sslContext.getSocketFactory();
    }

    private X509TrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private ExecutorService createCachedThreadPool() {
        return Executors.newCachedThreadPool(r -> {
            String name = "httpclient";
            Thread t = new Thread(null, r, name, 0, false);
            t.setDaemon(true);
            return t;
        });
    }

    public static Builder builder() {
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
        return new HttpRequest(this, this.setting, setting);
    }

    public static final class Builder {
        private final HttpClientSetting setting;

        public Builder() {
            setting = new HttpClientSetting();
        }

        public Builder baseUrl(String baseUrl) {
            setting.setUrl(baseUrl);
            return this;
        }

        public Builder retries(Integer retries) {
            setting.setRetries(retries);
            return this;
        }

        public Builder defaultHeaders(Boolean enabled) {
            setting.defaultHeaders = enabled;
            return this;
        }

        public Builder readTimeout(Integer milliseconds) {
            setting.setReadTimeout(milliseconds);
            return this;
        }

        public Builder writeTimeout(Integer milliseconds) {
            setting.setWriteTimeout(milliseconds);
            return this;
        }

        public Builder callTimeout(Integer milliseconds) {
            setting.setCallTimeout(milliseconds);
            return this;
        }

        public Builder cipherSuites(List<String> cipherSuites) {
            setting.setCipherSuites(cipherSuites);
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

        public Builder core(HttpClientCore core) {
            setting.setCore(core);
            return this;
        }

        public HttpClient build() {
            return new HttpClient(setting);
        }

        public Builder tlsVersions(List<String> tlsVersions) {
            setting.setTlsVersions(tlsVersions);
            return this;
        }
    }


}
