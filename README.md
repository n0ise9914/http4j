# Usage

```java
HttpClient client = HttpClient.builder()
        .baseUrl("https://example.com")
        .disableDefaultHeaders()
        .retries(5)
        .connectTimeout(3000)
        .readTimeout(5000)
        .build();

HttpResponse response = client.post("/api/echo")
        .body("body".getBytes())
        .header("name", "value")
        .urlParam("name", "value")
        .multipart("name", "value")
        .retries(3)
        .skipResponseBody(true)
        .connectTimeout(3000)
        .readTimeout(5000)
        .execute();
```
