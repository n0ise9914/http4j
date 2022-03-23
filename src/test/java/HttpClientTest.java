import com.http4j.HttpClient;
import com.http4j.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpClientTest {

    @Test
    void testGet() {
        HttpClient client = HttpClient.builder().build();
        HttpResponse response = client.get("https://google.com").execute();
        assertEquals(response.status, 200);
    }

}
