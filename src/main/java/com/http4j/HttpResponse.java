package com.http4j;

import java.util.List;
import java.util.Map;

public class HttpResponse {

    public int status;
    public byte[] body;
    public Exception error;
    public Map<String, List<String>> headers;

}
