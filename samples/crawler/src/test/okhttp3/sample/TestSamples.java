package okhttp3.sample;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import static org.junit.Assert.assertEquals;

public class TestSamples{
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));}

    @After
    public void cleanUpStreams(){
        System.setOut(null);
    }

    @Test
    public void crawlOkhttp() throws IOException {
        OkHttpClient client= new OkHttpClient();
        Crawler crawler = new Crawler(client);
        crawler.fetch(HttpUrl.parse("http://square.github.io/okhttp/"));
        assertEquals("200: http://square.github.io/okhttp/ (network: 200 over http/1.1)\n", outContent.toString());

    }
}